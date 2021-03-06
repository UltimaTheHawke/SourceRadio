package com.joelchristophel.sourceradio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

class FileUtilities {

	private FileUtilities() {
	}

	/**
	 * Returns the lines of the specified file as a <code>List</code>.
	 * 
	 * @param path
	 *            - the path to a file
	 * @param includeBlankLines
	 *            - indicates whether or not to include blank lines in list
	 * @return the lines of the specified file or an empty list if the file was not found
	 */
	static List<String> getLines(String path, boolean includeBlankLines) {
		List<String> lines = new ArrayList<String>();
		File file = new File(path);
		if (file.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(new File(path)));) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (!line.trim().isEmpty() || includeBlankLines) {
						lines.add(line);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return lines;
	}

	static boolean fileHasLine(String path, String line, boolean commentAware) {
		List<String> lines = getLines(path, true);
		boolean hasLine = false;
		for (String fileLine : lines) {
			if (fileLine.matches(line + (commentAware ? "(\\s*//.*)?" : ""))) {
				hasLine = true;
				break;
			}
		}
		return hasLine;
	}

	/**
	 * Appends this line to the end of the specified file.
	 * 
	 * @param path
	 *            - the path to a file
	 * @param line
	 *            - the line to append
	 */
	static void appendLine(String path, String line) {
		trimFile(path);
		line = line.startsWith(System.lineSeparator()) ? line : System.lineSeparator() + line;
		try {
			Files.write(Paths.get(path), line.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
		trimFile(path);
	}

	/**
	 * Opens a thread to delete the specified file.
	 * 
	 * @param file
	 *            - the file to be deleted
	 */
	static void deleteFile(final File file) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (file.exists()) {
					file.delete();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	 * Removes blank lines from the beginning and end of the specified file.
	 * 
	 * @param path
	 *            - the path to a file
	 */
	static void trimFile(String path) {
		List<String> lines = getLines(path, true);
		int endingBlankLines = 0;
		for (int i = lines.size() - 1; i >= 0; i--) {
			if (lines.get(i).trim().isEmpty()) {
				endingBlankLines++;
			} else {
				break;
			}
		}
		String trimmedText = "";
		boolean textHasStarted = false;
		for (int i = 0; i < lines.size() - endingBlankLines; i++) {
			if (!lines.get(i).trim().isEmpty()) {
				textHasStarted = true;
			}
			if (textHasStarted) {
				trimmedText += lines.get(i) + (i == lines.size() - endingBlankLines - 1 ? "" : System.lineSeparator());
			}
		}
		File file = new File(path);
		file.delete();
		try {
			Files.write(Paths.get(path), trimmedText.getBytes(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Removes this line from the specified file.
	 * 
	 * @param path
	 *            - the path to a file
	 * @param line
	 *            - the line to remove
	 * @param commentAware
	 *            - indicates whether or not this method is aware that comments exist
	 * @returns <code>true</code> if the file contained the line to begin with; <code>false</code> otherwise
	 */
	static boolean removeLine(String path, String line, boolean commentAware) {
		boolean hasLine = fileHasLine(path, line, commentAware);
		if (hasLine) {
			List<String> lines = getLines(path, true);
			if (hasLine) {
				String text = "";
				for (int i = 0; i < lines.size(); i++) {
					if (!lines.get(i).matches(line + (commentAware ? "(\\s*//.*)?" : ""))) {
						text += lines.get(i) + System.lineSeparator();
					}
				}
				if (!text.isEmpty() && text.endsWith(System.lineSeparator())) {
					text = text.substring(0, text.length() - System.lineSeparator().length());
				}
				File file = new File(path);
				file.delete();
				try {
					Files.write(Paths.get(path), text.getBytes(), StandardOpenOption.CREATE);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return hasLine;
	}

	static String normalizeDirectoryPath(String path) {
		if (path != null && !path.endsWith(File.separator)) {
			path += File.separator;
		}
		return path;
	}
}