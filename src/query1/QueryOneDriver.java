package query1;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.TotalOrderPartitioner;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import utility.Properties;
import utility.WikiModificationFileInputFormat;

/**
 * Driver class for query three.
 * Note that the total order partitioner is used to achieve global sorting of keys before the reducers receive their inputs. 
 * 
 * @author vincentfung13
 */

public class QueryOneDriver extends Configured implements Tool {
	
	public int run(String[] args) throws Exception {
		Job job = Job.getInstance(getConf());
		job.setJobName("QueryOneDriver");
		job.setJarByClass(QueryOneDriver.class);
		job.setMapperClass(QueryOneMapper.class);
		job.setReducerClass(QueryOneReducer.class);
		job.setNumReduceTasks(Properties.NUM_REDUCER_TASK);
		
		job.setMapOutputKeyClass(LongWritable.class);
		job.setMapOutputValueClass(LongWritable.class);
		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(Text.class);
		
		job.setInputFormatClass(WikiModificationFileInputFormat.class);
		WikiModificationFileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		job.setPartitionerClass(TotalOrderPartitioner.class);
		Path partitionFile = new Path(Properties.PARTITIONING_PATH_QUERY_ONE);
		TotalOrderPartitioner.setPartitionFile(job.getConfiguration(), partitionFile);
		
		// Taking key samples from the input file
//		double pcnt = 10.0;
//		int numSamples = Properties.NUM_REDUCER_TASK;
//		int maxSplits = Properties.NUM_REDUCER_TASK - 1;
//		if (0 >= maxSplits)
//			maxSplits = Integer.MAX_VALUE;
//		InputSampler.Sampler<LongWritable, LongWritable> sampler = 
//				new InputSampler.RandomSampler<LongWritable, LongWritable>(pcnt, numSamples, maxSplits);
//        InputSampler.writePartitionFile(job, sampler);

		job.getConfiguration().set("earlierTimestamp", args[2]);
		job.getConfiguration().set("laterTimestamp", args[3]);
			

		job.submit();
		return (job.waitForCompletion(true)? 0 : 1);
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		conf.addResource(new Path(Properties.PATH_TO_CORESITE_CONF));
		conf.set("mapreduce.job.jar", Properties.PATH_TO_JAR);
		ToolRunner.run(conf, new QueryOneDriver(), args);
		
		System.out.println("INFO: Mapreduce job finsihed, printing out the results:");
		try {
			FileSystem fs = FileSystem.get(conf);
			Path jobOutputPath = new Path(args[1]);
		
			FileStatus[] status = fs.listStatus(jobOutputPath);
			for (int i = 0; i < status.length; i++) {
				BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(status[i].getPath())));
				String line;
                line = br.readLine();
                while (line != null){
                	System.out.println(line);
                    line = br.readLine();
                }
			}
		} catch (Exception e) {
			System.err.println("ERROR: File not found.");
		}	
	}
}
