/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xpn.xwiki.wysiwyg.client.plugin.importer.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RichTextArea;
import com.xpn.xwiki.wysiwyg.client.editor.Strings;

/**
 * Office Importer UI tab for importing clipboard content.
 * 
 * @version $Id$
 */
public class ClipboardImportTab extends Composite
{
    /**
     * Container panel.
     */
    private FlowPanel mainPanel;

    /**
     * Editor area (where the user can paste his content).
     */
    private RichTextArea editor;

    /**
     * Default constructor.
     */
    public ClipboardImportTab()
    {
        // Main container panel.
        mainPanel = new FlowPanel();

        // Info label.
        Panel infoLabel = new FlowPanel();
        infoLabel.setStyleName("xInfoLabel");
        infoLabel.add(new InlineLabel(Strings.INSTANCE.importerClipboardTabInfoLabel()));
        InlineLabel mandatoryLabel = new InlineLabel(Strings.INSTANCE.mandatory());
        mandatoryLabel.addStyleName("xMandatory");
        infoLabel.add(mandatoryLabel);
        Label helpLabel = new Label(Strings.INSTANCE.importerClipboardTabHelpLabel());
        helpLabel.setStyleName("xHelpLabel");
        mainPanel.add(infoLabel);
        mainPanel.add(helpLabel);

        // Editor panel.
        editor = new RichTextArea();
        editor.addStyleName("xImporterClipboardTabEditor");
        mainPanel.add(editor);

        // Finalize.
        initWidget(mainPanel);
    }

    /**
     * Clears the content of the editor rta.
     */
    public void resetEditor()
    {
        editor.setHTML("");
    }

    /**
     * @return the html content of editor.
     */
    public String getHtmlPaste()
    {
        return editor.getHTML();
    }
}
