package org.javaee7.batch.sample.chunk.mapper;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.awaitility.Duration.FIVE_HUNDRED_MILLISECONDS;
import static com.jayway.awaitility.Duration.ONE_MINUTE;
import static jakarta.batch.runtime.BatchRuntime.getJobOperator;
import static jakarta.batch.runtime.BatchStatus.COMPLETED;
import static jakarta.batch.runtime.BatchStatus.STARTED;
import static jakarta.batch.runtime.Metric.MetricType.COMMIT_COUNT;
import static jakarta.batch.runtime.Metric.MetricType.READ_COUNT;
import static jakarta.batch.runtime.Metric.MetricType.WRITE_COUNT;
import static org.javaee7.Libraries.awaitability;
import static org.javaee7.batch.sample.chunk.mapper.MyItemReader.totalReaders;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.jboss.shrinkwrap.api.asset.EmptyAsset.INSTANCE;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.JobExecution;
import jakarta.batch.runtime.Metric;
import jakarta.batch.runtime.StepExecution;

import org.javaee7.util.BatchTestHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The Batch specification provides a Chunk Oriented processing style. This style is defined by enclosing into a
 * transaction a set of reads, process and write operations via +jakarta.batch.api.chunk.ItemReader+,
 * +jakarta.batch.api.chunk.ItemProcessor+ and +jakarta.batch.api.chunk.ItemWriter+. Items are read one at a time, processed
 * and aggregated. The transaction is then committed when the defined +checkpoint-policy+ is triggered.
 *
 * Many batch processing problems can be solved with single threaded, single process jobs, but the Batch specification
 * allows for steps to be executed as a partitioned step, meaning that the step can be parallelized across multiple
 * threads. This is useful if you have some kind of bottleneck or if you can considerable boost your batch processing
 * performance by splitting the work to be done.
 *
 * You can define the number of partitions and the number of threads using a custom mapper. The custom mapper needs to
 * implement +jakarta.batch.api.partition.PartitionMapper+ and create a new +jakarta.batch.api.partition.PartitionPlan+ to
 * define the partitions behaviour. Each partition is required to receive a set of unique parameters that instruct it
 * into which data it should operate.
 *
 * Since each thread runs a separate copy of the step, chunking and checkpointing occur independently on each thread for
 * chunk type steps.
 *
 * include::myJob.xml[]
 *
 * A job is defined in the +myJob.xml+ file. Just a single step with a reader, a processor and a writer. This step also
 * defines that the step should be executed into a partition with a custom mapper:
 *
 * include::MyMapper[]
 *
 * The mapper defines 2 partitions and 2 threads. Properties for each partition define the data that is going to be
 * read. For the first partition we start on 1 and end on 10. For the second partition we start on 11 and end on 20. The
 * +MyItemReader+ will generate the data based on these properties.
 *
 * include::MyItemReader[]
 *
 * @author Roberto Cortez
 */
@RunWith(Arquillian.class)
public class BatchChunkMapperTest {
    /**
     * We're just going to deploy the application as a +web archive+. Note the inclusion of the following files:
     *
     * [source,file]
     * ----
     * /META-INF/batch-jobs/myJob.xml
     * ----
     *
     * The +myJob.xml+ file is needed for running the batch definition.
     */
    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = create(WebArchive.class)
            .addClass(BatchTestHelper.class)
            .addPackage("org.javaee7.batch.sample.chunk.mapper")
            .addAsWebInfResource(INSTANCE, ArchivePaths.create("beans.xml"))
            .addAsResource("META-INF/batch-jobs/myJob.xml")
            .addAsLibraries(awaitability());

        System.out.println(war.toString(true));

        return war;
    }

    /**
     * In the test, we're just going to invoke the batch execution and wait for completion. To validate the test
     * expected behaviour we need to query the +jakarta.batch.runtime.Metric+ object available in the step execution.
     *
     * The batch process itself will read and process 20 elements from numbers 1 to 20, but only write the odd
     * elements. Elements from 1 to 10 will be processed in one partition and elements from 11 to 20 in another
     * partition. Commits are executed after 3 elements are read by partition.
     *
     * @throws Exception an exception if the batch could not complete successfully.
     */
    @Test
    public void testBatchChunkMapper() throws Exception {
        JobOperator jobOperator = getJobOperator();
        Long executionId = jobOperator.start("myJob", new Properties());
        JobExecution jobExecution = jobOperator.getJobExecution(executionId);

        final JobExecution lastExecution = BatchTestHelper.keepTestAlive(jobExecution);

        await().atMost(ONE_MINUTE)
        .with().pollInterval(FIVE_HUNDRED_MILLISECONDS)
        .until(                                                                                                                                                                                      new Callable<Boolean>() { @Override public Boolean call() throws Exception {
            return lastExecution.getBatchStatus() != STARTED;                                                                                                                                        }}
         );

        List<StepExecution> stepExecutions = jobOperator.getStepExecutions(executionId);
        for (StepExecution stepExecution : stepExecutions) {
            if (stepExecution.getStepName().equals("myStep")) {
                Map<Metric.MetricType, Long> metricsMap = BatchTestHelper.getMetricsMap(stepExecution.getMetrics());

                // <1> The read count should be 20 elements. Check +MyItemReader+.
                assertEquals(20L, metricsMap.get(READ_COUNT).longValue());

                // <2> The write count should be 10. Only half of the elements read are processed to be written.
                assertEquals(10L, metricsMap.get(WRITE_COUNT).longValue());

                // Number of elements by the item count value on myJob.xml, plus an additional transaction for the
                // remaining elements by each partition.
                long commitCount = (10L / 3 + (10 % 3 > 0 ? 1 : 0)) * 2;

                // <3> The commit count should be 8. Checkpoint is on every 3rd read, 4 commits for read elements and 2 partitions.
                assertEquals(commitCount, metricsMap.get(COMMIT_COUNT).longValue());
            }
        }

        // <4> Make sure that all the partitions were created.
        assertEquals(2L, totalReaders.get());
        
        // <5> Job should be completed.
        assertEquals(COMPLETED, lastExecution.getBatchStatus());
    }
}
