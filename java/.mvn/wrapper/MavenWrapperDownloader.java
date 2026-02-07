/*
 * Copyright 2007-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Properties;

public class MavenWrapperDownloader {

  private static final String WRAPPER_VERSION = "3.2.0";

  private static final String DEFAULT_DOWNLOAD_URL =
      "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/"
          + WRAPPER_VERSION + "/maven-wrapper-" + WRAPPER_VERSION + ".jar";

  private static final String MAVEN_WRAPPER_PROPERTIES_PATH =
      ".mvn/wrapper/maven-wrapper.properties";

  public static void main(String[] args) {
    System.out.println("- Downloader started");
    File baseDirectory = new File(System.getProperty("maven.multiModuleProjectDirectory", "."));
    System.out.println("- Using base directory: " + baseDirectory.getAbsolutePath());
    File mavenWrapperPropertyFile = new File(baseDirectory, MAVEN_WRAPPER_PROPERTIES_PATH);
    String url = DEFAULT_DOWNLOAD_URL;
    if (mavenWrapperPropertyFile.exists()) {
      FileInputStream inputStream = null;
      try {
        inputStream = new FileInputStream(mavenWrapperPropertyFile);
        Properties mavenWrapperProperties = new Properties();
        mavenWrapperProperties.load(inputStream);
        url = mavenWrapperProperties.getProperty("wrapperUrl", url);
      } catch (IOException e) {
        System.out.println("- ERROR loading maven-wrapper.properties");
        e.printStackTrace();
      } finally {
        if (inputStream != null) {
          try {
            inputStream.close();
          } catch (IOException e) {
            System.out.println("- ERROR closing maven-wrapper.properties");
            e.printStackTrace();
          }
        }
      }
    }
    System.out.println("- Downloading from: " + url);
    File outputFile = new File(baseDirectory, ".mvn/wrapper/maven-wrapper.jar");
    if (!outputFile.getParentFile().exists()) {
      if (!outputFile.getParentFile().mkdirs()) {
        System.out.println("- ERROR creating output directory " + outputFile.getParentFile());
      }
    }
    System.out.println("- Downloading to: " + outputFile.getAbsolutePath());
    try {
      downloadFileFromURL(url, outputFile);
      System.out.println("Done");
      System.exit(0);
    } catch (Throwable e) {
      System.out.println("- Error downloading");
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void downloadFileFromURL(String urlString, File destination) throws Exception {
    if (System.getenv("MVNW_USERNAME") != null && System.getenv("MVNW_PASSWORD") != null) {
      String username = System.getenv("MVNW_USERNAME");
      char[] password = System.getenv("MVNW_PASSWORD").toCharArray();
      Authenticator.setDefault(new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(username, password);
        }
      });
    }
    URL website = new URL(urlString);
    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
    FileOutputStream fos = new FileOutputStream(destination);
    try {
      fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    } finally {
      fos.close();
    }
  }
}
