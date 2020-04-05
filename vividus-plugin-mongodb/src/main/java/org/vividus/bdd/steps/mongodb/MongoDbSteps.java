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

import java.util.Map;
import java.util.Set;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import org.apache.commons.lang3.Validate;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jbehave.core.annotations.When;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;

public class MongoDbSteps
{
    private final Map<String, String> connections;
    private final IBddVariableContext bddVariableContext;

    public MongoDbSteps(Map<String, String> connections, IBddVariableContext bddVariableContext)
    {
        this.connections = connections;
        this.bddVariableContext = bddVariableContext;
    }

    /**
     * Actions performed in the step:
     * <ul>
     *     <li>executes provided <b>command</b> against mongodb by the provided <b>connectionKey</b></li>
     *     <li>saves the command execution result into <b>variableName</b> variable in JSON format</li>
     * </ul>
     *
     * @param command command to perform e.g. <i>{ listCollections: 1, nameOnly: true }</i>
     * @param dbKey database name e.g. <i>users</i>
     * @param connectionKey key of particular connection under <b>mongodb.connection.</b> prefix
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A name of variable to assign the values from command execution result
     * @see <a href="https://docs.mongodb.com/manual/reference/command/">Database commands</a>
     */
    @When("I execute command `$command` against `$dbKey` database in `$connectionKey` mongodb and save result "
            + "to $scopes variable `$variableName`")
    public void executeCommand(String command, String dbKey, String connectionKey, Set<VariableScope> scopes,
            String variableName)
    {
        String connection = connections.get(connectionKey);
        Validate.validState(connection != null, "Connection with key '%s' does not exist", connectionKey);
        try (MongoClient client = MongoClients.create(connection))
        {
            MongoDatabase database = client.getDatabase(dbKey);
            Bson executable = Document.parse(command);
            Document document = database.runCommand(executable);
            bddVariableContext.putVariable(scopes, variableName, document.toJson());
        }
    }
}
