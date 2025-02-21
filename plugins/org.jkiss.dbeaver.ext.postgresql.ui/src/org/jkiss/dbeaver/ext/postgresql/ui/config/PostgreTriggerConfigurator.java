/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2021 DBeaver Corp and others
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

package org.jkiss.dbeaver.ext.postgresql.ui.config;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreProcedure;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreTrigger;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.model.edit.DBEObjectConfigurator;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.model.navigator.DBNModel;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntityType;
import org.jkiss.dbeaver.model.struct.DBSInstance;
import org.jkiss.dbeaver.model.struct.DBSObjectContainer;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.ui.UITask;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.CSmartSelector;
import org.jkiss.dbeaver.ui.editors.object.struct.EntityEditPage;

import java.util.Map;

/**
 * Postgre sequence configurator
 */
public class PostgreTriggerConfigurator implements DBEObjectConfigurator<PostgreTrigger> {

    //protected static final Log log = Log.getLog(PostgreTriggerConfigurator.class);

    @Override
    public PostgreTrigger configureObject(DBRProgressMonitor monitor, Object parent, PostgreTrigger trigger, Map<String, Object> options) {
        return new UITask<PostgreTrigger>() {

            @Override
            protected PostgreTrigger runTask() {
                TriggerEditPage editPage = new TriggerEditPage(trigger);
                if (!editPage.edit()) {
                    return null;
                }
                trigger.setName(editPage.getEntityName());
                trigger.setFunction(editPage.selectedFunction);
                return trigger;
            }
        }.execute();
    }

    public class TriggerEditPage extends EntityEditPage {

        PostgreTrigger trigger;
        CSmartSelector<PostgreProcedure> functionCombo;
        PostgreProcedure selectedFunction;

        TriggerEditPage(PostgreTrigger trigger) {
            super(trigger.getDataSource(), DBSEntityType.TRIGGER);
            this.trigger = trigger;
        }

        @Override
        protected Control createPageContents(Composite parent) {
            Composite pageContents = (Composite) super.createPageContents(parent);
            UIUtils.createControlLabel(pageContents, "Trigger function");
            functionCombo = new PostgreProcedureSelector(pageContents, parent);
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.widthHint = UIUtils.getFontHeight(functionCombo) * 30;
            functionCombo.setLayoutData(gd);

            // On macOS, the combo's down arrow is not shown unless you manually resize the page. The solution is to call layout()
            // https://github.com/dbeaver/dbeaver/issues/12651
            UIUtils.asyncExec(functionCombo::layout);

            return pageContents;
        }

        @Override
        public boolean isPageComplete() {
            return super.isPageComplete() && selectedFunction != null;
        }

        private class PostgreProcedureSelector extends CSmartSelector<PostgreProcedure> {
            private final Composite parent;

            PostgreProcedureSelector(Composite pageContents, Composite parent) {
                super(pageContents, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY, new LabelProvider() {
                    @Override
                    public Image getImage(Object element) {
                        return DBeaverIcons.getImage(DBIcon.TREE_PROCEDURE);
                    }

                    @Override
                    public String getText(Object element) {
                        if (element == null) {
                            return "N/A";
                        }
                        return ((PostgreProcedure) element).getFullQualifiedSignature();
                    }
                });
                this.parent = parent;
            }

            @Override
            protected void dropDown(boolean drop) {
                if (drop) {
                    DBNModel navigatorModel = DBWorkbench.getPlatform().getNavigatorModel();
                    DBNDatabaseNode dsNode = navigatorModel.getNodeByObject(trigger.getDatabase());
                    if (dsNode != null) {
                        DBNNode curNode = selectedFunction == null ? null
                                : navigatorModel.getNodeByObject(selectedFunction);
                        DBNNode node = DBWorkbench.getPlatformUI().selectObject(parent.getShell(),
                                "Select function for ", dsNode, curNode,
                                new Class[]{ DBSInstance.class, DBSObjectContainer.class, PostgreProcedure.class },
                                new Class[]{ PostgreProcedure.class }, null);
                        if (node instanceof DBNDatabaseNode
                                && ((DBNDatabaseNode) node).getObject() instanceof PostgreProcedure) {
                            functionCombo.removeAll();
                            selectedFunction = (PostgreProcedure) ((DBNDatabaseNode) node).getObject();
                            functionCombo.addItem(selectedFunction);
                            functionCombo.select(selectedFunction);
                            updatePageState();
                        }
                    }
                }
            }
        }
    }
}
