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

package ru.myweek_end.maven.versor;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

/**
 * Обновление версии pom.
 * 
 * @author <a href="https://myweek-end.ru/">Моя неделя завершилась</a>
 * @author <a href="mailto:drum@pisem.net">Алексей Кляузер</a>
 * @since 0.0.1.2
 * @version 0.0.1.5
 */
@Mojo(name = "set")
public class Versor extends AbstractMojo {

  /**
   * Проект Maven.
   * 
   * @since 0.0.1.2
   */
  @Parameter(property = "project", readonly = true)
  private MavenProject project;

  /**
   * Сессия Maven.
   * 
   * @since 0.0.1.2
   */
  @Parameter(property = "session", readonly = true)
  private MavenSession session;

  /**
   * Менеджер плагинов Maven.
   * 
   * @since 0.0.1.2
   */
  @Component
  private BuildPluginManager pluginManager;

  /**
   * Корневая директория проекта Maven.
   * 
   * @since 0.0.1.2
   */
  @Parameter(property = "project.basedir", readonly = true, required = true)
  private File baseDirectory;

  /**
   * Директория проекта Maven в которой идёт исполнение.
   * 
   * @since 0.0.1.2
   */
  @Parameter(property = "session.executionRootDirectory", readonly = true, required = true)
  private File executionRootDirectory;

  /**
   * Требуется ли исполнять плагин только в корневой директории.
   * 
   * @since 0.0.1.2
   */
  @Parameter(property = "runOnlyAtExecutionRoot", defaultValue = "true")
  private boolean runOnlyAtExecutionRoot;

  /**
   * Окружение для выполнения плагинов Maven.
   * 
   * @since 0.0.1.2
   */
  private MojoExecutor.ExecutionEnvironment pluginEnv;

  /**
   * Версия продукта.
   * 
   * @since 0.0.1.5
   */
  @Parameter(property = "product.version")
  private String productVersion;

  /**
   * Стратегия определения версии продукта.
   * 
   * @since 0.0.1.5
   */
  @Parameter(property = "versor.strategy", defaultValue = "STRICT_TAG")
  private VersorStrategy strategy;

  /**
   * Паттерн главной версии продукта.
   * 
   * @since 0.0.1.5
   */
  @Parameter(property = "versor.main.pattern", required = true, defaultValue = "\\d+\\.\\d+\\.\\d+")
  private String mainVersionPattern;

  /**
   * Вызов цели.
   * 
   * @since 0.0.1.2
   * @throws MojoExecutionException
   *           При проблемах вызова цели
   */
  @Override
  public final void execute() throws MojoExecutionException {
    pluginEnv = new ExecutionEnvironment(project, session, pluginManager);
    if (executionRootDirectory.equals(baseDirectory) || !runOnlyAtExecutionRoot) {
      this.buildnumber();
      this.set();
    }
  }

  /**
   * Сборка информации для номера версии.
   * 
   * @since 0.0.1.2
   * @throws MojoExecutionException
   *           При проблемах вызова плагина
   */
  private void buildnumber() throws MojoExecutionException {
    Plugin buildnumberPlugin = plugin("ru.concerteza.buildnumber", "maven-jgit-buildnumber-plugin",
        "1.2.10");
    final Xpp3Dom cfg = configuration();
    executeMojo(buildnumberPlugin, goal("extract-buildnumber"), cfg, pluginEnv);
  }

  private boolean isRelease(String tags, String version) {
    switch (this.strategy) {
    case PROD_BRANCH:
      String branch = project.getProperties().getProperty("git.branch");
      branch.contains("prod");
      break;
    case STRICT_TAG:
      String[] tagArray = tags.split(";");
      for (String tag : tagArray) {
        if (tag.equals("v" + version)) {
          return true;
        }
      }
      break;
    }
    return false;
  }

  /**
   * Запись версии в pom файл.
   * 
   * @since 0.0.1.2
   * @throws MojoExecutionException
   *           При проблемах вызова плагина
   */
  private void set() throws MojoExecutionException {
    Plugin versionsPlugin = plugin("org.codehaus.mojo", "versions-maven-plugin", "2.2");
    String productVersion = buildVersion();
    String commitsCount = project.getProperties().getProperty("git.commitsCount");
    String version = productVersion + "." + commitsCount;
    String tags = project.getProperties().getProperty("git.tag");
    if (!isRelease(tags, version)) {
      version = version + "-SNAPSHOT";
    }
    getLog().info("version =\"" + version + "\"");
    final Xpp3Dom cfg = configuration(element(name("newVersion"), version));
    if (versionsPlugin != null && cfg != null) {
      executeMojo(versionsPlugin, goal("set"), cfg, pluginEnv);
    }
  }

  /**
   * Вернуть строку с версией
   * 
   * @since 0.0.1.5
   * @return Строка с версией
   */
  private String buildVersion() {
    if (this.productVersion == null) {
      Pattern pattern = getMainVersionPattern();
      Matcher matcher;
      switch (this.strategy) {
      case PROD_BRANCH:
        String branch = project.getProperties().getProperty("git.branch");
        matcher = pattern.matcher(branch);
        if (matcher.find()) {
          this.productVersion = matcher.group();
        } else {
          this.productVersion = "0.0.0";
        }
        break;
      case STRICT_TAG:
        matcher = pattern.matcher(this.project.getVersion());
        if (matcher.find()) {
          this.productVersion = matcher.group();
        } else {
          this.productVersion = "0.0.0";
        }
        break;
      }
    }
    return this.productVersion;
  }

  /**
   * Вернуть паттерн главной версии продукта.
   * 
   * @since 0.0.1.5
   * @return Паттерн.
   */
  private Pattern getMainVersionPattern() {
    return Pattern.compile(this.mainVersionPattern);
  }

}
