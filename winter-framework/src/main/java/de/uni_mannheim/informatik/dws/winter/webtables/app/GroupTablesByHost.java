/*
 * Copyright (c) 2017 Data and Web Science Group, University of Mannheim, Germany (http://dws.informatik.uni-mannheim.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package de.uni_mannheim.informatik.dws.winter.webtables.app;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.slf4j.Logger;

import com.beust.jcommander.Parameter;

import de.uni_mannheim.informatik.dws.winter.utils.Executable;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.specialised.uri.UriParser;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class GroupTablesByHost extends Executable {

	@Parameter(names = "-tables", required=true)
	private String input;
	
	@Parameter(names = "-result", required=true)
	private String output;
	
	@Parameter(names = "-copy")
	private boolean copy;
	
	private static final Logger logger = WinterLogManager.getLogger();
	
	public static void main(String[] args) throws Exception {
		
		GroupTablesByHost app = new GroupTablesByHost();
		
		if(app.parseCommandLine(GroupTablesByHost.class, args)) {
			
			app.run();
			
		}
		
	}
	
	public void run() throws Exception {
		
		File in = new File(input);
		File out = new File(output);
		
		if(!in.exists()) {
			logger.error(String.format("%s does not exist!", in.getAbsolutePath()));
			return;
		}
		
		if(!out.exists()) {
			out.mkdirs();
		}
		
		logger.info(in.getAbsolutePath());
		
		LinkedList<File> files = new LinkedList<>();
		// LinkedList<File> toList = new LinkedList<>(Arrays.asList(in.listFiles()));
		
		// first list all files that need to be processed (including subdirectories)
		// while(!toList.isEmpty()) {
		// 	File f = toList.poll();
			
		// 	if(f.isDirectory()) {
		// 		logger.info(f.getAbsolutePath());
		// 		toList.addAll(Arrays.asList(f.listFiles()));
		// 	} else {
		// 		files.add(f);
		// 	}
		// }

		Path path = in.toPath();
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

			long last = System.currentTimeMillis();

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				files.add(file.toFile());
				if(System.currentTimeMillis()-last>10000) {
					logger.info(String.format("Listing files, %d so far ...", files.size()));
					last = System.currentTimeMillis();
				}
				return FileVisitResult.CONTINUE;
			}
		});
		
		UriParser p = new UriParser();
		long start = System.currentTimeMillis();
		long lastTime = 0;
		int ttlFiles = files.size();
		int last = 0;
		
		while(!files.isEmpty()) {
			File f = files.poll();
			
			// if(f.isDirectory()) {
			// 	// this cannot happen
			// 	throw new Exception();
			// } else {
				Table t = p.parseTable(f);
				
				File newFile = new File(new File(out, getHostName(t)), t.getPath());
				newFile.getParentFile().mkdir();
				
				if(copy) {
					Files.copy(f.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} else {
					Files.createLink(newFile.toPath(), f.toPath());
				}
				
			// }
			

			if(System.currentTimeMillis()-lastTime>=10000) {
				
				int tasks = ttlFiles;
				int done = ttlFiles - files.size();
				
				long soFar = System.currentTimeMillis() - start;
				long pauseTime = System.currentTimeMillis() - lastTime;
				long left = (long) (((float) soFar / done) * (tasks - done));
				float itemsPerSecAvg = (float)done / (float)(soFar / 1000.0f);
				float itemsPerSecNow = (float)(done - last) / (pauseTime / 1000.0f);
				
				if((((float) soFar) / done)==Float.POSITIVE_INFINITY)
				{
					left = -1;
				}
				String ttl = DurationFormatUtils.formatDuration(soFar, "HH:mm:ss.S");
				String remaining = DurationFormatUtils.formatDuration(left, "HH:mm:ss.S");
				
				logger.info(String.format("%,d of %,d tasks completed after %s. Avg: %.2f items/s, Current: %.2f items/s, %s left.", done, tasks, ttl, itemsPerSecAvg, itemsPerSecNow, remaining));
				
				last = done;
				lastTime = System.currentTimeMillis();
			}
		}
		
		logger.error(String.format("%,d tasks completed after %s.", ttlFiles, DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "HH:mm:ss.S")));
		
		logger.info("done.");
	}
	
	public String getHostName(Table t) throws URISyntaxException {
		URI uri = new URI(t.getContext().getUrl());
		
		String host = uri.getHost();
		
		return host;
	}
}
