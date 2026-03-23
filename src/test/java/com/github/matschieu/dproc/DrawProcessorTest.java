package com.github.matschieu.dproc;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matschieu
 */
public class DrawProcessorTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(DrawProcessorTest.class);

	DrawProcessor drawProcessor;
	ByteArrayOutputStream byteStream;
	PrintStream printStream;

	@BeforeAll
	static void init() {
		DrawProcessor drawProcessor = DrawProcessor.build();

		for(int i = 0; i < drawProcessor.getNumberOfDraws(); i++) {
			System.out.println(drawProcessor.getDraw(i));
		}
	}

	@BeforeEach
	void initTest(TestInfo testInfo) throws UnsupportedEncodingException {
		drawProcessor = DrawProcessor.build();
		byteStream = new ByteArrayOutputStream();
		printStream = new PrintStream(byteStream, true, "UTF-8");
		LOGGER.info("Running {}", testInfo.getDisplayName());
	}

	@Test
	void testActivation() throws IOException {
		assertThat(drawProcessor.isActive()).isTrue();
		drawProcessor.displayRandomly(printStream);
		assertThat(byteStream.size()).isGreaterThan(0);
		byteStream.reset();

		drawProcessor.deactivate();
		assertThat(drawProcessor.isActive()).isFalse();
		drawProcessor.displayRandomly(printStream);
		assertThat(byteStream.size()).isZero();
		byteStream.reset();

		drawProcessor.activate();
		assertThat(drawProcessor.isActive()).isTrue();
		drawProcessor.displayRandomly(printStream);
		assertThat(byteStream.size()).isGreaterThan(0);
		byteStream.reset();
	}

	@Test
	void testGetDraw() {
		assertThat(drawProcessor.getDraw(-1)).isNull();
		for(int i = 0; i < drawProcessor.getNumberOfDraws(); i++) {
			assertThat(drawProcessor.getDraw(i)).isNotNull();
		}
		assertThat(drawProcessor.getDraw(drawProcessor.getNumberOfDraws())).isNull();
	}

	@Test
	void testDisplay() {
		drawProcessor.display(printStream, 0);
		String draw1 = byteStream.toString();
		String draw2 = drawProcessor.getDraw(0);
		assertThat(draw1).isEqualTo(draw2);

		draw2 = drawProcessor.getDraw(1);
		assertThat(draw1).isNotEqualTo(draw2);
	}

	@Test
	void testDisplayRandomly() {
		int[] result = new int[drawProcessor.getNumberOfDraws()];

		for(int i = 0; i < result.length; i++) {
			result[i] = 0;
		}

		for(int iteration = 0; iteration < 10; iteration++) {
			drawProcessor.displayRandomly(printStream);
			String draw = byteStream.toString();

			for(int i = 0; i < drawProcessor.getNumberOfDraws(); i++) {
				if (draw.equals(drawProcessor.getDraw(i))) {
					result[i]++;
					break;
				}
			}

			byteStream.reset();
		}

		int numberOdDrawDisplayed = 0;
		for(int i = 0; i < result.length; i++) {
			if (result[i] > 0) {
				numberOdDrawDisplayed++;
			}
		}

		assertThat(numberOdDrawDisplayed).isGreaterThan(1);
	}

	static void moveFile(String fileFrom, String fileTo) {
		try {
			Path path = Paths.get(fileFrom);
			Path newPath = Paths.get(fileTo);
			Files.move(path, newPath, StandardCopyOption.REPLACE_EXISTING);
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	@Test
	void testNoConfFile() {
		// Rename the files dproc.conf to make DrawProcessor not able to read the file
		moveFile("target/test-classes/dproc.conf", "target/test-classes/dproc_.conf");
		moveFile("target/classes/dproc.conf", "target/classes/dproc_.conf");

		DrawProcessor.reload();

		assertThat(DrawProcessor.build().getNumberOfDraws()).isZero();
		assertThat(DrawProcessor.build().getDraw(1)).isNull();

		moveFile("target/test-classes/dproc_.conf", "target/test-classes/dproc.conf");
		moveFile("target/classes/dproc_.conf", "target/classes/dproc.conf");

		DrawProcessor.reload();
	}

	@Test
	void testEmptyConfFile() throws IOException {
		// Rename the files dproc.conf to make DrawProcessor not able to read the file
		moveFile("target/test-classes/dproc.conf", "target/test-classes/dproc_.conf");
		Files.createFile(Paths.get("target/test-classes/dproc.conf"));

		DrawProcessor.reload();

		assertThat(DrawProcessor.build().getNumberOfDraws()).isZero();
		assertThat(DrawProcessor.build().getDraw(1)).isNull();

		moveFile("target/test-classes/dproc_.conf", "target/test-classes/dproc.conf");

		DrawProcessor.reload();
	}
}
