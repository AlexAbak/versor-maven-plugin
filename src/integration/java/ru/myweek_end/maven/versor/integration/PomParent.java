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

import javax.xml.bind.annotation.XmlElement;

/**
 * Указание родительского проекта.
 * 
 * @author <a href="https://myweek-end.ru/">Моя неделя завершилась</a>
 * @author <a href="mailto:drum@pisem.net">Алексей Кляузер</a>
 * @since 0.0.1.2
 * @version 0.0.1.2
 */
public class PomParent {

  /**
   * Идентификатор группы.
   * 
   * @since 0.0.1.2
   */
  @XmlElement(name = "groupId", namespace = Pom.ns)
  public String groupId;

  /**
   * Идентификатор артефакта.
   * 
   * @since 0.0.1.2
   */
  @XmlElement(name = "artifactId", namespace = Pom.ns)
  public String artifactId;

  /**
   * Версия.
   * 
   * @since 0.0.1.2
   */
  @XmlElement(name = "version", namespace = Pom.ns)
  public String version;

  /**
   * Относительный путь.
   * 
   * @since 0.0.1.2
   */
  @XmlElement(name = "relativePath", namespace = Pom.ns)
  public String relativePath;

}
