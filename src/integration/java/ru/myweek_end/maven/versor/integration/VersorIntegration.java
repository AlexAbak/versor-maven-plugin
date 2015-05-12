/*
 * Copyright © 2015 "Алексей Кляузер <drum@pisem.net>" Все права защищены.
 */

/*
 * This file is part of maven-versor-plugin.
 *
 * maven-versor-plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * maven-versor-plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with maven-versor-plugin.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Этот файл — часть maven-versor-plugin.
 *
 * maven-versor-plugin - свободная программа: вы можете перераспространять ее
 * и/или изменять ее на условиях Стандартной общественной лицензии GNU в том
 * виде, в каком она была опубликована Фондом свободного программного
 * обеспечения; либо версии 3 лицензии, либо (по вашему выбору) любой более
 * поздней версии.
 *
 * maven-versor-plugin распространяется в надежде, что она будет полезной,
 * но БЕЗО ВСЯКИХ ГАРАНТИЙ; даже без неявной гарантии ТОВАРНОГО ВИДА
 * или ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Подробнее см. в Стандартной
 * общественной лицензии GNU.
 *
 * Вы должны были получить копию Стандартной общественной лицензии GNU
 * вместе с этой программой. Если это не так, см.
 * <http://www.gnu.org/licenses/>.
 */

package ru.myweek_end.maven.versor.integration;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.codehaus.plexus.util.IOUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * Интеграционный тест обновления версии pom.
 * 
 * @author <a href="https://myweek-end.ru/">Моя неделя завершилась</a>
 * @author <a href="mailto:drum@pisem.net">Алексей Кляузер</a>
 * @since 0.0.1.2
 * @version 0.0.1.2
 */
public class VersorIntegration {

  /**
   * Чтение pom файла.
   * 
   * @since 0.0.1.2
   * @param source
   *          Файл для чтения
   * @return Десериализованный pom
   * @throws JAXBException
   *           при проблемах десериализации
   */
  private Pom readPom(File source) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Pom.class);
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    return (Pom) unmarshaller.unmarshal(source);
  }

  /**
   * Размер буфера для чтения потока.
   * 
   * @since 0.0.1.2
   */
  private static final int BUFFER_SIZE = 2048;

  /**
   * Признак завершения потока.
   * 
   * @since 0.0.1.2
   */
  private static final int EOF_MARK = -1;

  /**
   * Копирование входного потока в выходной..
   * 
   * @since 0.0.1.2
   * @param source
   *          Входной поток
   * @param dest
   *          Выходной поток
   * @throws IOException
   *           при проблемах с потоками
   */
  private void copyInputToOutput(InputStream source, OutputStream dest)
      throws IOException {
    byte[] buffer = new byte[BUFFER_SIZE];
    int bytesRead = EOF_MARK;
    while ((bytesRead = source.read(buffer)) != EOF_MARK) {
      dest.write(buffer, 0, bytesRead);
    }
  }

  /**
   * Запуск процесса и генерация исключения в случае появления ошибок.
   * 
   * @since 0.0.1.2
   * @param processBuilder
   *          Заполненные свойства создаваемого процесса
   * @throws Exception
   *           в случае наличия ошибок
   */
  private void throwProcess(ProcessBuilder processBuilder) throws Exception {
    System.out.println(processBuilder.command().toString());
    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();

    InputStream stdout = process.getInputStream();
    InputStreamReader isrStdout = new InputStreamReader(stdout);
    BufferedReader brStdout = new BufferedReader(isrStdout);

    InputStream stderr = process.getErrorStream();
    InputStreamReader isrStderr = new InputStreamReader(stderr);
    BufferedReader brStderr = new BufferedReader(isrStderr);

    String line = null;

    while ((line = brStdout.readLine()) != null) {
      System.out.println(" >> " + line);
    }

    StringBuilder builder = new StringBuilder();
    while ((line = brStderr.readLine()) != null) {
      builder.append(line);
      builder.append("\n");
    }
    process.waitFor();
    if (builder.length() != 0) {
      throw new Exception(builder.toString());
    }
  }

  /**
   * Подготовка репозитория maven и распаковка репозитория git.
   * 
   * @since 0.0.1.2
   * @throws Exception
   *           при проблемах подготовки
   */
  @Before
  public void setUp() throws Exception {

    Path project = new File("./").getCanonicalFile().toPath();
    File pomFile = project.resolve("pom.xml").toFile();
    Path target = project.resolve("target");
    File rep = target.resolve("rep").toFile();

    rep.mkdirs();
    Pom pom = readPom(pomFile);

    String version = pom.version;

    String mvn;
    if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
      mvn = "mvn.bat";
    } else {
      mvn = "mvn";
    }

    ProcessBuilder processBuilder;

    processBuilder = new ProcessBuilder(mvn, "install:install-file",
        "-DpomFile=" + pomFile.toString(), "-Dfile="
            + target.resolve("versor-maven-plugin-" + version + ".jar")
                .toString(), "-Dsources="
            + target.resolve("versor-maven-plugin-" + version + "-sources.jar")
                .toString(), "-Djavadoc="
            + target.resolve("versor-maven-plugin-" + version + "-javadoc.jar")
                .toString(), "-DgroupId=ru.myweek-end.maven",
        "-DartifactId=versor-maven-plugin", "-Dversion=" + version,
        "-Dpackaging=jar", "-DlocalRepositoryPath=" + rep.toString(),
        "-DcreateChecksum=true");
    this.throwProcess(processBuilder);

    Path resources = project.resolve("src/integration/resources");
    ZipFile zipFile = new ZipFile(resources.resolve("testrep.zip").toFile());
    try {
      for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries
          .hasMoreElements();) {
        ZipEntry entry = entries.nextElement();
        File file = target.resolve(entry.getName()).toFile();
        if (entry.isDirectory()) {
          file.mkdirs();
        } else {
          InputStream source = zipFile.getInputStream(entry);
          FileOutputStream output = new FileOutputStream(file);
          if ("testrep/pom.xml".equals(entry.getName())) {

            String repReg = rep.toString();
            repReg = repReg.replaceAll(":\\\\", "/");
            repReg = repReg.replaceAll("\\\\", "/");
            System.out.println(repReg);

            String pomContent = IOUtil.toString(source, "UTF-8");
            pomContent = pomContent.replaceAll("#repository", repReg);
            pomContent = pomContent.replaceAll("#version", version);
            InputStream stream = new ByteArrayInputStream(
                pomContent.getBytes("UTF-8"));
            copyInputToOutput(stream, output);
          } else {
            copyInputToOutput(source, output);
          }
        }
        System.out.println(entry.getName());
      }
    } finally {
      zipFile.close();
    }
  }

  /**
   * Запуск теста установки версии.
   * 
   * @since 0.0.1.2
   * @throws Exception
   *           при проблемах вызова maven
   */
  @Test
  public void test() throws Exception {
    Path project = new File("./").getCanonicalFile().toPath();
    Path target = project.resolve("target");
    Path testrep = target.resolve("testrep");
    File pomFile = testrep.resolve("pom.xml").toFile();

    String mvn;
    if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
      mvn = "mvn.bat";
    } else {
      mvn = "mvn";
    }

    ProcessBuilder processBuilder;
    processBuilder = new ProcessBuilder(mvn, "-f", pomFile.toString(),
        "versor:set");
    processBuilder = processBuilder.directory(testrep.toFile());
    this.throwProcess(processBuilder);

    Pom pom = readPom(pomFile);
    assertEquals("x.y.1", pom.version);
  }

}
