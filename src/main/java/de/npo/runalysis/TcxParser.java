package de.npo.runalysis;

import static javax.xml.stream.XMLStreamConstants.*;

import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 * Copyright (C) 2015 Niklas Polke<br/>
 * <br/>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.<br/>
 * <br/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Niklas Polke
 */
public class TcxParser {

	public static final String USAGE = "usage: " + TcxParser.class.getName() + " <filename>";

	public static final String TIME_FORMAT = "HH:mm:ss.SSS";
	public static final int FACTOR_SECONDS_TO_MILLIS = 1000;

	public static final String ELEMENT_DISTANCE = "DistanceMeters";
	public static final String ELEMENT_TIME = "TotalTimeSeconds";

	private String filename;

	private double distanceInMeters;

	private double durationInSeconds;

	public TcxParser(final String filename) {
		this.filename = filename;
	}

	public void readFile() {
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader xmlReader = factory.createXMLStreamReader(new FileInputStream(filename));

			boolean withinDistance = false;
			boolean withinTotalTimeSeconds = false;

			while (xmlReader.hasNext()) {
				switch (xmlReader.getEventType()) {
				case END_DOCUMENT:
					xmlReader.close();
					break;

				case START_ELEMENT:
					if (ELEMENT_DISTANCE.equalsIgnoreCase(xmlReader.getLocalName())) {
						withinDistance = true;
					} else if (ELEMENT_TIME.equalsIgnoreCase(xmlReader.getLocalName())) {
						withinTotalTimeSeconds = true;
					}
					break;

				case CHARACTERS:
					if (withinDistance) {
						distanceInMeters = Double.parseDouble(xmlReader.getText());
					} else if (withinTotalTimeSeconds) {
						durationInSeconds += Double.parseDouble(xmlReader.getText());
					}
					break;

				case END_ELEMENT:
					if (ELEMENT_DISTANCE.equalsIgnoreCase(xmlReader.getLocalName())) {
						withinDistance = false;
					} else if (ELEMENT_TIME.equalsIgnoreCase(xmlReader.getLocalName())) {
						withinTotalTimeSeconds = false;
					}
					break;
				}
				xmlReader.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public double getDistance() {
		return distanceInMeters;
	}

	public double getDuration() {
		return durationInSeconds;
	}

	public static String formatAsDuration(final double seconds) {
		long milliseconds = (long) (seconds * FACTOR_SECONDS_TO_MILLIS);
		long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(hours);
		long secs = TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes);
		milliseconds = milliseconds % FACTOR_SECONDS_TO_MILLIS;
		return String.format("%02d:%02d:%02d.%03d", hours, minutes, secs, milliseconds);
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println(USAGE);
		} else {
			TcxParser parser = new TcxParser(args[0]);
			System.out.print("Reading file \"" + args[0] + "\"...");
			parser.readFile();
			System.out.println(" done.");
			System.out.println();
			System.out.println("track duration: " + formatAsDuration(parser.getDuration()));
			System.out.println("track distance: " + parser.getDistance());
		}
	}
}
