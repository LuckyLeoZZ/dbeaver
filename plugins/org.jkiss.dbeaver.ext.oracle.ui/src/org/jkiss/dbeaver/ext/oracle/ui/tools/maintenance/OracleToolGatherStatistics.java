/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2021 DBeaver Corp and others
 * Copyright (C) 2011-2012 Eugene Fradkin (eugene.fradkin@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ext.oracle.ui.tools.maintenance;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.oracle.model.OracleTableBase;
import org.jkiss.dbeaver.ext.oracle.model.OracleTableIndex;
import org.jkiss.dbeaver.ext.oracle.tasks.OracleTasks;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.tasks.ui.wizard.TaskConfigurationWizardDialog;
import org.jkiss.dbeaver.ui.navigator.NavigatorUtils;
import org.jkiss.dbeaver.ui.tools.IUserInterfaceTool;
import org.jkiss.utils.collections.CollectionUtils;

import java.util.Collection;
import java.util.List;

/**
 * Gather statistics
 */
public class OracleToolGatherStatistics implements IUserInterfaceTool
{
    @Override
    public void execute(IWorkbenchWindow window, IWorkbenchPart activePart, Collection<DBSObject> objects) throws DBException
    {
        List<OracleTableBase> tables = CollectionUtils.filterCollection(objects, OracleTableBase.class);
        if (!tables.isEmpty()) {
            TaskConfigurationWizardDialog.openNewTaskDialog(
                    window,
                    NavigatorUtils.getSelectedProject(),
                    OracleTasks.TASK_TABLE_GATHER_STATISTICS,
                    new StructuredSelection(objects.toArray()));
        } else {
            List<OracleTableIndex> databases = CollectionUtils.filterCollection(objects, OracleTableIndex.class);
            if (!databases.isEmpty()) {
                TaskConfigurationWizardDialog.openNewTaskDialog(
                        window,
                        NavigatorUtils.getSelectedProject(),
                        OracleTasks.TASK_INDEX_GATHER_STATISTICS,
                        new StructuredSelection(objects.toArray()));
            }
        }
    }
}
