/**
 * (C) Copyright IBM Corporation 2017, 2020.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openliberty.tools.common.plugins.config;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

public class LooseConfigData extends XmlDocument {

    private String projectRoot = null;
    private String sourceOnDiskName = null;

    /**
     * Set both projectRoot and sourceOnDiskName to control the name used when an element is added.
     * @param root  the name of the directory that contains the actual project resources
     */
    public void setProjectRoot(String root) {
        projectRoot = root;
    }

    /** 
     * Set both projectRoot and sourceOnDiskName to control the name used when an element is added.
     * @param name  the name to use in the config file as the apparent location of the resource
     */
    public void setSourceOnDiskName(String name) {
        sourceOnDiskName = name;
    }

    public LooseConfigData() throws ParserConfigurationException {
        createDocument("archive");
    }

    public void toXmlFile(File xmlFile) throws Exception {
        writeXMLDocument(xmlFile);
    }

    public Element addDir(File src, String target) throws DOMException, IOException {
        if (src != null && src.exists() && src.isDirectory()) {
            return addDir(doc.getDocumentElement(), src, target);
        }
        return null;
    }
    
    public Element addDir(Element parent, File src, String target) throws DOMException, IOException {
        if (src != null && src.exists() && src.isDirectory()) {
            Element child = doc.createElement("dir");
            return addElement(parent, child, src, target);
        }
        return null;
    }
    
    public Element addFile(File src, String target) throws DOMException, IOException {
        if (src != null && src.exists() && src.isFile()) {
            return addFile(doc.getDocumentElement(), src, target);
        }
        return null;
    }
    
    public Element addFile(Element parent, File src, String target) throws DOMException, IOException {
        return addFile(parent, src, target, null);
    }
    
    public Element addFile(Element parent, File src, String target, File copyDirectory) throws DOMException, IOException {
        if (src != null && src.exists() && src.isFile()) {
            Element child = doc.createElement("file");
            if(copyDirectory != null && copyDirectory.exists() && copyDirectory.isDirectory() &&
                    !src.getCanonicalPath().contains(copyDirectory.getCanonicalPath())) {
                // Create a unique subdirectory based on timestamp so we don't get any overwritten files
                File copyFileDirectory = new File(copyDirectory, Long.toString(System.nanoTime()));
                copyFileDirectory.mkdir();
                
                // Copy the file into the directory
                File copyFile = new File(copyFileDirectory, src.getName());
                FileUtils.copyFile(src, copyFile);
                
                return addElement(parent, child, copyFile, target);
            }
            else {
                return addElement(parent, child, src, target);
            }
        }
        return null;
    }
    
    public Element addArchive(String target) {
        return addArchive(doc.getDocumentElement(), target);
    }
    
    public Element addArchive(Element parent, String target) {
        Element newArchive = doc.createElement("archive");
        addElement(parent, newArchive, target);
        return newArchive;
    }
    
    public Element addArchive(File dir, String target) throws DOMException, IOException {
        Element newArchive = addArchive(target);
        addElement(newArchive, doc.createElement("dir"), dir, "/");
        return newArchive;
    }
    
    public Element getDocumentRoot() {
        return doc.getDocumentElement();
    }
    
    private Element addElement(Element parent, Element child, File src, String target) throws DOMException, IOException {
        String name = src.getCanonicalPath();
        if (sourceOnDiskName != null && projectRoot != null && name.startsWith(projectRoot)) {
            child.setAttribute("sourceOnDisk", sourceOnDiskName + name.substring(projectRoot.length()));
        } else {
            child.setAttribute("sourceOnDisk", src.getCanonicalPath());
        }
        return addElement(parent, child, target);
    }
    
    private Element addElement(Element parent, Element child, String target) {
        child.setAttribute("targetInArchive", target);
        parent.appendChild(child);
        return child;
    }
}
