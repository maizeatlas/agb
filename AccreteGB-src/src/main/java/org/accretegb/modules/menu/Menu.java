package org.accretegb.modules.menu;

/*
 * Licensed to Openaccretegb-common under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Openaccretegb-common licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author nkumar
 * This is an extended Menu class
 * which contains all subMenus
 */
public class Menu extends JMenu {

    private static final long serialVersionUID = 1L;
    private List<MenuItem> subMenus = new LinkedList<MenuItem>();

    public Menu(String label) {
        super(label);
    }

    public List<MenuItem> getSubMenus() {
        return subMenus;
    }

    public void setSubMenus(List<MenuItem> subMenus) {
        this.subMenus = subMenus;
    }

}
