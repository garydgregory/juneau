// ***************************************************************************************************************************
// * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file *
// * distributed with this work for additional information regarding copyright ownership.  The ASF licenses this file        *
// * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance            *
// * with the License.  You may obtain a copy of the License at                                                              *
// *                                                                                                                         *
// *  http://www.apache.org/licenses/LICENSE-2.0                                                                             *
// *                                                                                                                         *
// * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an  *
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the        *
// * specific language governing permissions and limitations under the License.                                              *
// ***************************************************************************************************************************
package org.apache.juneau.doc.internal;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Utility for generating the overview.html page.
 */
public class DocGenerator {

	/**
	 * Entry point.
	 *
	 * @param args Not used
	 */
	public static void main(String[] args) {
		try {
			long startTime = System.currentTimeMillis();

			String template = IOUtils.readFile("src/main/resources/overview-template.html");

			Topics topics = new Topics(new File("src/main/resources/Topics"));
			ReleaseNotes releaseNotes = new ReleaseNotes(new File("src/main/resources/ReleaseNotes"));

			StringBuilder toc = new StringBuilder(), contents = new StringBuilder();
			for (PageFile pf1 : topics.pageFiles) {
				toc
					.append("\t<li><p class='toc2 ").append(pf1.tags).append("'><a class='doclink' href='#").append(pf1.fullId).append("'>").append(pf1.title).append("</a></p>\n");
				contents
					.append("\n")
					.append("<!-- ==================================================================================================== -->\n\n")
					.append("<h2 class='topic ").append(pf1.tags).append("' onclick='toggle(this)'><a href='#").append(pf1.fullId).append("' id='").append(pf1.fullId).append("'>").append(pf1.fullNumber).append(" - ").append(pf1.title).append("</a></h2>\n")
					.append("<div class='topic'>").append("<!-- START: ").append(pf1.fullNumber).append(" - " ).append(pf1.fullId).append(" -->\n")
					.append(pf1.contents).append("\n");

				if (! pf1.pageFiles.isEmpty()) {

					toc.append("\t<ol>\n");

					for (PageFile pf2 : pf1.pageFiles) {

						toc
							.append("\t\t<li><p class='").append(pf2.tags).append("'><a class='doclink' href='#").append(pf2.fullId).append("'>").append(pf2.title).append("</a></p>\n");
						contents
							.append("\n")
							.append("<!-- ==================================================================================================== -->\n\n")
							.append("<h3 class='topic ").append(pf2.tags).append("' onclick='toggle(this)'><a href='#").append(pf2.fullId).append("' id='").append(pf2.fullId).append("'>").append(pf2.fullNumber).append(" - ").append(pf2.title).append("</a></h3>\n")
							.append("<div class='topic'>").append("<!-- START: ").append(pf2.fullNumber).append(" - " ).append(pf2.fullId).append(" -->\n")
							.append(pf2.contents).append("\n");

						if (! pf2.pageFiles.isEmpty()) {
							toc.append("\t\t<ol>\n");

							for (PageFile pf3 : pf2.pageFiles) {

								toc
									.append("\t\t\t<li><p class='").append(pf3.tags).append("'><a class='doclink' href='#").append(pf3.fullId).append("'>").append(pf3.title).append("</a></p>\n");
								contents
									.append("\n")
									.append("<!-- ==================================================================================================== -->\n\n")
									.append("<h4 class='topic ").append(pf3.tags).append("' onclick='toggle(this)'><a href='#").append(pf3.fullId).append("' id='").append(pf3.fullId).append("'>").append(pf3.fullNumber).append(" - ").append(pf3.title).append("</a></h4>\n")
									.append("<div class='topic'>").append("<!-- START: ").append(pf3.fullNumber).append(" - " ).append(pf3.fullId).append(" -->\n")
									.append(pf3.contents).append("\n")
									.append("</div>").append("<!-- END: ").append(pf3.fullNumber).append(" - ").append(pf3.fullId).append(" -->\n");
							}

							toc.append("\t\t</ol>\n");
						}

						contents
							.append("</div>").append("<!-- END: ").append(pf2.fullNumber).append(" - ").append(pf2.fullId).append(" -->\n");
					}

					toc.append("\t</ol>\n");
				}

				contents
					.append("</div>").append("<!-- END: ").append(pf1.fullNumber).append(" - ").append(pf1.fullId).append(" -->\n");
			}

			StringBuilder tocRn = new StringBuilder(), rn = new StringBuilder();

			for (ReleaseFile rf : releaseNotes.releaseFiles) {
				tocRn
					.append("<li><p><a class='doclink' href='#").append(rf.version).append("'>").append(rf.title).append("</a></p>\n");
				rn
					.append("\n")
					.append("<!-- ==================================================================================================== -->\n\n")
					.append("<h3 class='topic' onclick='toggle(this)'><a href='#").append(rf.version).append("' id='").append(rf.version).append("'>").append(rf.title).append("</a></h3>\n")
					.append("<div class='topic'>").append("<!-- START: ").append(rf.version).append(" -->\n")
					.append(rf.contents).append("\n")
					.append("</div>").append("<!-- END: ").append(rf.version).append(" -->\n");
			}

			template = template.replace("<!--{TOC-CONTENTS}-->", toc.toString()).replace("<!--{CONTENTS}-->", contents.toString()).replace("<!--{TOC-RELEASE-NOTES}-->", tocRn).replace("<!--{RELEASE-NOTES}-->", rn);

			IOUtils.writeFile("src/main/javadoc/overview.html", template);

			System.err.println("Generated target/overview.html in "+(System.currentTimeMillis()-startTime)+"ms");

			startTime = System.currentTimeMillis();
			for (File f : new File("src/main/javadoc/doc-files").listFiles())
				Files.delete(f.toPath());
			for (File f : topics.docFiles)
				Files.copy(f.toPath(), Paths.get("src/main/javadoc/doc-files", f.getName()));
			for (File f : releaseNotes.docFiles)
				Files.copy(f.toPath(), Paths.get("src/main/javadoc/doc-files", f.getName()));
			System.err.println("Copied doc-files in "+(System.currentTimeMillis()-startTime)+"ms");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static class Topics {
		Set<PageFile> pageFiles = new TreeSet<>();
		List<File> docFiles = new ArrayList<>();

		public Topics(File topicsDir) throws IOException {
			for (File f : topicsDir.listFiles()) {
				if (f.isFile())
					pageFiles.add(new PageFile(null, f, docFiles));
				else if (f.isDirectory() && f.getName().contains("doc-files"))
					docFiles.addAll(Arrays.asList(f.listFiles()));
			}
		}
	}

	static class ReleaseNotes {
		Set<ReleaseFile> releaseFiles = new TreeSet<>();
		List<File> docFiles = new ArrayList<>();

		public ReleaseNotes(File releaseNotesDir) throws IOException {
			for (File f : releaseNotesDir.listFiles()) {
				if (f.isFile())
					releaseFiles.add(new ReleaseFile(f));
				else if (f.isDirectory() && f.getName().contains("doc-files"))
					docFiles.addAll(Arrays.asList(f.listFiles()));
			}
		}
	}

	static class PageFile implements Comparable<PageFile> {
		String idWithNum, id, num, fullId, title, contents;
		String number, fullNumber;
		String tags = "";

		Set<PageFile> pageFiles = new TreeSet<>();

		PageFile(PageFile parent, File f, List<File> docFiles) throws IOException {
			try {
				String n = f.getName();
				idWithNum = n.substring(0, n.lastIndexOf('.'));
				num = n.substring(0, n.indexOf('.'));
				id = idWithNum.substring(n.indexOf('.') + 1);
				fullId = (parent == null ? "" : parent.fullId + ".") + id;
				number = n.substring(0, n.indexOf('.')).replaceAll("^[0]+", "");
				fullNumber = (parent == null ? "" : parent.fullNumber + ".") + number;
				String s = IOUtils.read(f);
				int i = s.indexOf("-->");
				s = s.substring(i+4).trim();
				i = s.indexOf("\n");
				title = s.substring(0, i);
				if (title.startsWith("{")) {
					tags = title.substring(1, title.indexOf('}'));
					title = title.substring(tags.length()+2).trim();
				}
				contents = s.substring(i).trim();
			} catch (Exception e) {
				throw new RuntimeException("Problem with file " + f.getAbsolutePath());
			}

			for (File d : f.getParentFile().listFiles()) {
				if (d.isDirectory() && d.getName().startsWith(num + '.')) {
					if (! d.getName().matches(idWithNum))
						throw new RuntimeException("Unmatched file/directory: file=["+f.getName()+"], directory=["+d.getName()+"]");
					for (File f2 : d.listFiles()) {
						if (f2.isFile())
							pageFiles.add(new PageFile(this, f2, docFiles));
						else if (f2.isDirectory() && f2.getName().contains("doc-files"))
							docFiles.addAll(Arrays.asList(f2.listFiles()));
					}
				}
			}
		}

		@Override
		public int compareTo(PageFile o) {
			return this.idWithNum.compareTo(o.idWithNum);
		}
	}

	static class ReleaseFile implements Comparable<ReleaseFile> {
		String name, version, title, contents;

		ReleaseFile(File f) throws IOException {
			name = f.getName();
			name = name.substring(0, name.lastIndexOf('.'));
			version = name.replaceAll("[0](\\d+)", "$1");
			String s = IOUtils.read(f);
			int i = s.indexOf("-->");
			s = s.substring(i+4).trim();
			i = s.indexOf("\n");
			title = s.substring(0, i);
			contents = s.substring(i).trim();
		}

		@Override
		public int compareTo(ReleaseFile o) {
			return this.name.compareTo(o.name);
		}
	}
}
