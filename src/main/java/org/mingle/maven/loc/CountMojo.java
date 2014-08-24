/**
 * Copyright (c) 2014, Mingle. All rights reserved.
 */
package org.mingle.maven.loc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 *
 * @author <a href="mailto:jinminglei@yeah.net">mingle</a>
 * @date 2014年8月24日
 * @version 1.0
 * @goal count
 */
public class CountMojo extends AbstractMojo {

	private static final String[] INCLUDES_DEFAULT = { "java", "xml",
			"properties" };

	/**
	 * @parameter expression="${project.basedir}"
	 * @required
	 * @readonly
	 */
	private File basedir;

	/**
	 * @parameter expression="${project.build.sourceDirectory}"
	 * @required
	 * @readonly
	 */
	private File sourceDirectory;

	/**
	 * @parameter expression="${project.build.testSourceDirectory}"
	 * @required
	 * @readonly
	 */
	private File testSourceDirectory;

	/**
	 * @parameter expression="${project.build.resources}"
	 * @required
	 * @readonly
	 */
	private List<Resource> resources;

	/**
	 * @parameter expression="${project.build.testResources}"
	 * @required
	 * @readonly
	 */
	private List<Resource> testResources;

	/**
	 * The file types which will be included for counting
	 * 
	 * @parameter
	 */
	private String[] includes;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (includes == null || includes.length == 0) {
			includes = INCLUDES_DEFAULT;
		}

		try {
			this.countDir(sourceDirectory);

			this.countDir(testSourceDirectory);

			for (Resource resource : resources) {
				this.countDir(new File(resource.getDirectory()));
			}

			for (Resource resource : testResources) {
				this.countDir(new File(resource.getDirectory()));
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to count lines of code.",
					e);
		}
	}

	private void countDir(File dir) throws IOException {
		if (!dir.exists()) {
			return;
		}

		List<File> collected = new ArrayList<File>();

		this.collectFiles(collected, dir);

		int lines = 0;

		for (File sourceFile : collected) {
			lines += this.countLine(sourceFile);
		}

		String path = dir.getAbsolutePath().substring(
				basedir.getAbsolutePath().length());

		this.getLog().info(
				path + ": " + lines + " lines of code in " + collected.size()
						+ " files");

	}

	private void collectFiles(List<File> collected, File file) {
		if (file.isFile()) {
			for (String include : includes) {
				if (file.getName().endsWith("." + include)) {
					collected.add(file);

					break;
				}
			}
		} else {
			for (File sub : file.listFiles()) {
				this.collectFiles(collected, sub);
			}
		}
	}

	private int countLine(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));

		int line = 0;

		try {
			while (reader.ready()) {
				reader.readLine();

				line++;
			}
		} finally {
			reader.close();
		}

		return line;
	}
}
