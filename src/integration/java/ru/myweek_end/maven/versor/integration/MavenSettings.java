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
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Схема maven settings.xml.
 * 
 * @author <a href="https://myweek-end.ru/">Моя неделя завершилась</a>
 * @author <a href="mailto:drum@pisem.net">Алексей Кляузер</a>
 * @since 0.0.1.3
 * @version 0.0.1.3
 */
@XmlRootElement(name = "settings", namespace = MavenSettings.ns)
public class MavenSettings {

  /**
   * Пространство имён maven settings.xml.
   * 
   * @since 0.0.1.2
   */
  public static final String ns = "http://maven.apache.org/SETTINGS/1.0.0";

  /**
   * Пространство имён XMLSchema-instance.
   * 
   * @since 0.0.1.2
   */
  // public static final String xsi = "http://www.w3.org/2001/XMLSchema-instance";

  /**
   * Пространство имён maven settings.xml.
   * 
   * @since 0.0.1.2
   */
  public static final String nsLocation = "http://maven.apache.org/SETTINGS/1.0.0 "
       + "http://maven.apache.org/xsd/settings-1.0.0.xsd";

  /**
   * Версия модели.
   * 
   * @since 0.0.1.2
   */
  //@XmlAttribute(name = "schemaLocation", namespace = MavenSettings.xsi)
  //public String schemaLocation = MavenSettings.nsLocation;

  /**
   * Локальный репозиторий.
   * 
   * @since 0.0.1.2
   */
  @XmlElement(name = "localRepository", namespace = MavenSettings.ns)
  public String localRepository;

}
