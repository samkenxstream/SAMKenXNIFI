/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.processors.iceberg;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import java.util.List;

public class IcebergUtils {

    /**
     * Loads configuration files from the provided paths.
     *
     * @param configFilePaths list of config file paths separated with comma
     * @return merged configuration
     */
    public static Configuration getConfigurationFromFiles(List<String> configFilePaths) {
        final Configuration conf = new Configuration();
        if (configFilePaths != null) {
            for (final String configFile : configFilePaths) {
                conf.addResource(new Path(configFile.trim()));
            }
        }
        return conf;
    }
}
