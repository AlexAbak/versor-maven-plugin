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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
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
  private final Pom readPom(final File source) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Pom.class);
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    return (Pom) unmarshaller.unmarshal(source);
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
  private final void throwProcess(final ProcessBuilder processBuilder) throws Exception {
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
   * Очистка каталога.
   *
   * @since 0.0.1.3
   * @throws Exception
   *           при проблемах очистки
   */
  public final void erase(final File dir) {
    if (dir.exists() && dir.isDirectory()) {
      for (File item : dir.listFiles()) {
        if (item.isDirectory()) {
          erase(item);
        }
        item.delete();
      }
    }
  }

  /**
   * Распаковка zip архива.
   *
   * @since 0.0.1.3
   * @param sourceFile
   *          Исходный zip файл
   * @param destDir
   *          каталог назначения
   * @throws ZipException
   *           при проблемах с архивом
   * @throws IOException
   *           при проблемах с файловой системой
   */
  private final void UnZip(final File sourceFile, final Path destDir) throws ZipException,
      IOException {
    ZipFile zipFile = new ZipFile(sourceFile);
    try {
      for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();) {
        ZipEntry entry = entries.nextElement();
        File file = destDir.resolve(entry.getName()).toFile();
        if (entry.isDirectory()) {
          file.mkdirs();
        } else {
          InputStream source = zipFile.getInputStream(entry);
          FileOutputStream output = new FileOutputStream(file);
          IOUtil.copy(source, output);
        }
      }
    } finally {
      zipFile.close();
    }
  }

  /**
   * Подготовка репозитория maven.
   *
   * @since 0.0.1.3
   * @throws JAXBException
   *           при проблемах сохранения настроек
   * @throws IOException
   *           при проблемах с файловой системой
   * @throws ZipException
   *           при проблемах с архивом
   */
  private final File setUpMaven(final Path resources, final Path target) throws JAXBException,
      ZipException, IOException {
    Path m2 = target.resolve(".m2");
    erase(m2.toFile());
    UnZip(resources.resolve("m2.zip").toFile(), target);
    Path repository = m2.resolve("repository");
    repository.toFile().mkdirs();
    MavenSettings mavenSettings = new MavenSettings();
    mavenSettings.localRepository = repository.toString();

    JAXBContext jaxbContext = JAXBContext.newInstance(MavenSettings.class);
    Marshaller marshaller = jaxbContext.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, MavenSettings.nsLocation);
    File settingsXml = m2.resolve("settings.xml").toFile();
    marshaller.marshal(mavenSettings, settingsXml);

    return settingsXml;
  }

  /**
   * Путь к проекту.
   *
   * @since 0.0.1.3
   */
  private Path project;

  /**
   * Путь к ресурсам.
   *
   * @since 0.0.1.3
   */
  private Path resources;

  /**
   * Путь к папке сборки.
   *
   * @since 0.0.1.3
   */
  private Path target;

  /**
   * Настройки локального maven.
   *
   * @since 0.0.1.3
   */
  private File settingsXml;

  /**
   * Имя исполнимого файла maven.
   *
   * @since 0.0.1.3
   */
  private String mvn;

  /**
   * Подготовка репозитория maven и распаковка репозитория git.
   *
   * @since 0.0.1.2
   * @throws Exception
   *           при проблемах подготовки
   */
  @Before
  public void setUp() throws Exception {

    project = new File("./").getCanonicalFile().toPath();
    resources = project.resolve("src/integration/resources");
    target = project.resolve("target");
    settingsXml = setUpMaven(resources, target);

    File pomFile = project.resolve("pom.xml").toFile();
    Pom pom = readPom(pomFile);
    String version = pom.version;

    if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
      mvn = "mvn.bat";
    } else {
      mvn = "mvn";
    }

    ProcessBuilder processBuilder;

    processBuilder = new ProcessBuilder(
        mvn,
        "install:install-file",
        "-s",
        settingsXml.toString(),
        "-DpomFile=" + pomFile.toString(),
        "-Dfile=" + target.resolve("versor-maven-plugin-" + version + ".jar").toString(),
        "-Dsources=" + target.resolve("versor-maven-plugin-" + version + "-sources.jar").toString(),
        "-Djavadoc=" + target.resolve("versor-maven-plugin-" + version + "-javadoc.jar").toString(),
        "-DgroupId=ru.myweek-end.maven", "-DartifactId=versor-maven-plugin",
        "-Dversion=" + version, "-Dpackaging=jar", "-DcreateChecksum=true");
    this.throwProcess(processBuilder);

    erase(target.resolve("testrep").toFile());
    UnZip(resources.resolve("testrep.zip").toFile(), target);

    File testRepPom0 = target.resolve("testrep/pom.xml").toFile();
    File testRepPom1 = target.resolve("testrep/pom1.xml").toFile();
    testRepPom0.renameTo(testRepPom1);
    InputStream source = new FileInputStream(testRepPom1);
    String pomContent = IOUtil.toString(source, "UTF-8");
    pomContent = pomContent.replaceAll("#version", version);
    InputStream stream = new ByteArrayInputStream(pomContent.getBytes("UTF-8"));
    FileOutputStream output = new FileOutputStream(testRepPom0);
    IOUtil.copy(stream, output);
    testRepPom1.delete();
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
    Path testrep = target.resolve("testrep");
    File pomFile = testrep.resolve("pom.xml").toFile();

    ProcessBuilder processBuilder;
    processBuilder = new ProcessBuilder(mvn, "-s", settingsXml.toString(), "-f",
        pomFile.toString(), "versor:set");
    processBuilder = processBuilder.directory(testrep.toFile());
    this.throwProcess(processBuilder);

    Pom pom = readPom(pomFile);
    assertEquals("x.y.1-SNAPSHOT", pom.version);

    processBuilder = new ProcessBuilder("git", "tag", "-a", "vx.y.1", "-m", "'release'");
    processBuilder = processBuilder.directory(testrep.toFile());
    this.throwProcess(processBuilder);

    processBuilder = new ProcessBuilder(mvn, "-s", settingsXml.toString(), "-f",
        pomFile.toString(), "versor:set");
    processBuilder = processBuilder.directory(testrep.toFile());
    this.throwProcess(processBuilder);

    pom = readPom(pomFile);
    assertEquals("x.y.1", pom.version);

  }

}
