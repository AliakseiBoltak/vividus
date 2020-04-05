/*
 * Copyright 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.bdd.steps.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.Map;
import java.util.Set;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;

@RunWith(PowerMockRunner.class)
public class MongoDbStepsTests
{
    private static final String LOCAL_KEY = "localKey";
    private static final String COMMAND = "{ listCollections: 1, nameOnly: true }";
    private static final String VARIABLE_KEY = "variableKey";

    @Test
    public void testExecuteCommandNoConnection()
    {
        IBddVariableContext context = mock(IBddVariableContext.class);
        MongoDbSteps steps = new MongoDbSteps(Map.of(), context);
        Exception exception = Assertions.assertThrows(IllegalStateException.class,
            () -> steps.executeCommand(COMMAND, LOCAL_KEY, LOCAL_KEY, Set.of(VariableScope.STORY), VARIABLE_KEY));
        assertEquals("Connection with key 'localKey' does not exist", exception.getMessage());
    }

    @PrepareForTest(MongoClients.class)
    @Test
    public void testExecuteCommand()
    {
        mockStatic(MongoClients.class);
        MongoClient client = mock(MongoClient.class);
        MongoDatabase database = mock(MongoDatabase.class);
        Document document = mock(Document.class);

        String connection = "mongodb://0.0.0.0:27017";
        IBddVariableContext context = mock(IBddVariableContext.class);
        MongoDbSteps steps = new MongoDbSteps(Map.of(LOCAL_KEY, connection), context);

        when(MongoClients.create(connection)).thenReturn(client);
        when(client.getDatabase(LOCAL_KEY)).thenReturn(database);
        when(database.runCommand(Document.parse(COMMAND))).thenReturn(document);
        String json = "{}";
        when(document.toJson()).thenReturn(json);

        steps.executeCommand(COMMAND, LOCAL_KEY, LOCAL_KEY, Set.of(VariableScope.STORY), VARIABLE_KEY);

        verify(context).putVariable(Set.of(VariableScope.STORY), VARIABLE_KEY, json);
    }
}
