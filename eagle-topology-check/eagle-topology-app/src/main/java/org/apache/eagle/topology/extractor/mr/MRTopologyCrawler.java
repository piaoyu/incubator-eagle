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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.eagle.topology.extractor.mr;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.tuple.Values;
import org.apache.eagle.topology.TopologyCheckAppConfig;
import org.apache.eagle.topology.TopologyCheckMessageId;
import org.apache.eagle.topology.TopologyConstants;
import org.apache.eagle.topology.extractor.TopologyEntityParserResult;
import org.apache.eagle.topology.extractor.TopologyCrawler;
import org.apache.eagle.topology.resolver.TopologyRackResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MRTopologyCrawler implements TopologyCrawler {

    private static final Logger LOG = LoggerFactory.getLogger(MRTopologyCrawler.class);

    private MRTopologyEntityParser parser;
    private SpoutOutputCollector outputCollector;

    public MRTopologyCrawler(TopologyCheckAppConfig config, TopologyRackResolver rackResolver, SpoutOutputCollector collector) {
        this.parser = new MRTopologyEntityParser(config.dataExtractorConfig.site, config.mrConfig, rackResolver);
        this.outputCollector = collector;
    }

    @Override
    public void extract() {
        long updateTimestamp = System.currentTimeMillis();
        TopologyEntityParserResult result = parser.parse(updateTimestamp);
        if (result == null || result.getMasterNodes().isEmpty()) {
            LOG.warn("No data fetched");
            return;
        }
        TopologyCheckMessageId messageId = new TopologyCheckMessageId(TopologyConstants.TopologyType.MR, updateTimestamp);
        this.outputCollector.emit(new Values(TopologyConstants.MR_INSTANCE_SERVICE_NAME, result), messageId);
    }

}
