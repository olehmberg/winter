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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_mannheim.informatik.dws.winter.preprocessing.units;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author domi
 */
public class UnitParser {

    public static List<Unit> units = new ArrayList<>();
    
    public static Double parseUnit(String value, String unitInformation) {
        for (Unit unit : units) {
            if (!unitInformation.isEmpty()) {
                if (unitInformation.toLowerCase().equals(unit.getName())
                        || unit.getAbbreviations().contains(unitInformation.toLowerCase())) {
                    value = value.replaceAll("[^0-9\\,\\.\\-Ee\\+]", "");
                    Double valueBeforeTransformation = Double.parseDouble(value);
                    return valueBeforeTransformation * unit.getFactor();
                }
            } else {
                String nonNumberPart = value.replaceAll("[0-9\\,\\.\\-Ee\\+]", "");
                if (nonNumberPart.toLowerCase().equals(unit.getName())
                        || unit.getAbbreviations().contains(nonNumberPart.toLowerCase())) {
                    value = value.replaceAll("[^0-9\\,\\.\\-Ee\\+]", "");
                    Double valueBeforeTransformation = Double.parseDouble(value);
                    return valueBeforeTransformation * unit.getFactor();
                } else {
                    value = value.replaceAll("[^0-9\\,\\.\\-Ee\\+]", "");
                    Double valueBeforeTransformation = Double.parseDouble(value);
                    return valueBeforeTransformation;
                }
            }
        }
        return null;
    }

    public static Double transformUnit(String value, Unit unit) throws ParseException {
        value = value.replaceAll("[^0-9\\,\\.\\-Ee\\+]", "");
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        Number number = format.parse(value);
        Double valueBeforeTransformation = number.doubleValue();
        return valueBeforeTransformation * unit.getFactor();
    }

    public static Unit checkUnit(String value) {
        for (Unit unit : units) {
//            if (!header.isEmpty()) {
//                if (header.toLowerCase().equals(unit.getName())
//                        || unit.getAbbreviations().contains(header.toLowerCase())
//                        || extractUnitAbbrFromHeader(header).toLowerCase().equals(unit.getName())
//                        || unit.getAbbreviations().contains(extractUnitAbbrFromHeader(header).toLowerCase())) {
//                    return unit;
//                }
//            }
            String nonNumberPart = value.replaceAll("[0-9\\,\\.\\-Ee\\+]", "");
            if (nonNumberPart.toLowerCase().equals(unit.getName())
                    || unit.getAbbreviations().contains(nonNumberPart.toLowerCase())) {
                return unit;
            }
        }
        return null;
    }

    private static Pattern unitInHeaderPattern = Pattern.compile(".*\\((.*)\\).*");
    private static Pattern dotPattern = Pattern.compile("\\.");

    public static Unit parseUnitFromHeader(String header) {
        String unitName = extractUnitAbbrFromHeader(header).toLowerCase();

        for (Unit unit : units) {
            if (!header.isEmpty()) {
                if (header.toLowerCase().equals(unit.getName())
                        || unit.getAbbreviations().contains(header.toLowerCase())
                        || unitName.equals(unit.getName())
                        || unit.getAbbreviations().contains(unitName)) {
                    return unit;
                }
            }
        }

        return null;
    }

    private static String extractUnitAbbrFromHeader(String header) {
        try {
            //if (header.matches(".*\\(.*\\).*")) {
            Matcher m = unitInHeaderPattern.matcher(header);
            if (m.matches()) {
                String unit = m.group(1);

                return dotPattern.matcher(unit).replaceAll("");
                //return header.substring(header.indexOf("(") + 1, header.indexOf(")")).replaceAll("\\.", "");
            }
        } catch (Exception e) {
        }

        return header;
    }

    static {
    	initialiseUnits();
    }
    
    private static void initialiseUnits() {
    	synchronized (units) {
            if (units.isEmpty()) {
		    	try {
		    	
			        URI uri = UnitParser.class.getResource("Units/Convertible").toURI();
			        Path myPath;
			        if (uri.getScheme().equals("jar")) {
			            FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
			            myPath = fileSystem.getPath("/resources");
			        } else {
			            myPath = Paths.get(uri);
			        }
		        
					Files.walkFileTree(myPath, new HashSet<FileVisitOption>(), 1, new FileVisitor<Path>() {
		
						@Override
						public FileVisitResult preVisitDirectory(Path dir,
								BasicFileAttributes attrs) throws IOException {
							return FileVisitResult.CONTINUE;
						}
		
						@Override
						public FileVisitResult visitFile(Path file,
								BasicFileAttributes attrs) throws IOException {
							//System.out.println(file.toFile().getName());
							units.addAll(readConvertibleUnit(file.toFile()));
							return FileVisitResult.CONTINUE;
						}
		
						@Override
						public FileVisitResult visitFileFailed(Path file, IOException exc)
								throws IOException {
							return FileVisitResult.CONTINUE;
						}
		
						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException exc)
								throws IOException {
							return FileVisitResult.CONTINUE;
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
		    	catch (URISyntaxException e) {
					e.printStackTrace();
				}
            }
    	}
    }

    private static Set<Unit> readConvertibleUnit(File unitPath) {
        Set<Unit> unitsOfFile = new HashSet<>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(unitPath), "UTF8"));
            String fileLine = in.readLine();
            while (fileLine != null) {
                Unit currentUnit = new Unit();
                String[] parts = fileLine.split("\\|");
                currentUnit.setName(parts[0].replace("\"", ""));
                //List<String> abbs = new ArrayList();
                HashSet<String> abbs = new HashSet<>();
                String[] subUnitsStrs = parts[1].split(",");
                for (String s : subUnitsStrs) {
                    abbs.add(s.replace("\"", ""));
                }
                currentUnit.setAbbreviations(abbs);
                if (parts.length < 3) {
                    currentUnit.setFactor(1.0);
                } else {
                    currentUnit.setFactor(Double.parseDouble(parts[2]));
                }
                unitsOfFile.add(currentUnit);
                fileLine = in.readLine();
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return unitsOfFile;
    }
}
