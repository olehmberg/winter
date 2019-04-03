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

package de.uni_mannheim.informatik.dws.winter.utils;

import java.time.Duration;
import java.time.LocalDateTime;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;

/**
 * This class can be used to log and observe the progress of a method.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 */
public class ProgressReporter {

	private int done = 0;
	private LocalDateTime lastTime;
	private int total = 0;
	private LocalDateTime start;
	private String message;
	
	private static final Logger logger = WinterLogManager.getLogger("progress");

	public ProgressReporter(int totalElements, String message) {
		total = totalElements;
		start = LocalDateTime.now();
		lastTime = start;
		this.message = message;
	}
	
	public ProgressReporter(int totalElements, String message, int processedElements) {
		total = totalElements;
		start = LocalDateTime.now();
		lastTime = start;
		this.message = message;
		this.done = processedElements;
	}

	public void incrementProgress() {
		done++;
	}

	public void report() {
		// report status every second
		LocalDateTime now = LocalDateTime.now();
		long durationSoFar = Duration.between(start, now).toMillis();
		if ((Duration.between(lastTime, now).toMillis()) > 1000) {
			if(total>0) {
				logger.info(String.format(
					"%s: %,d / %,d elements completed (%.2f%%) after %s",
					message, done, total,
					(double) done / (double) total * 100,
					DurationFormatUtils.formatDurationHMS(durationSoFar)));
			} else {
				logger.info(String.format(
					"%s: %,d elements completed after %s",
					message, done,
					DurationFormatUtils.formatDurationHMS(durationSoFar)));
			}
			lastTime = now;
		}
	}

	public int getProcessedElements() {
		return done;
	}

	public void setProcessedElements(int done) {
		this.done = done;
	}

	public LocalDateTime getLastTime() {
		return lastTime;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public LocalDateTime getStart() {
		return start;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	
}
