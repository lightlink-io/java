package io.lightlink.translator;

/*
 * #%L
 * LightLink Core
 * %%
 * Copyright (C) 2015 - 2017 Vitaliy Shevchuk
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


import io.lightlink.core.ScriptRunner;
import io.lightlink.facades.SQLFacade;
import io.lightlink.sql.SQLHandler;
import junit.framework.TestCase;

import javax.script.ScriptException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class ScriptTranslatorTest extends TestCase{

    public void test() throws IOException, ScriptException, SQLException {
        String translated = new ScriptTranslator().translate("insert into x (1,2,:(date.DD/MM/YYYY HH/MM/SSS)p.date);");
        assertTrue(translated.contains(",'(date.DD/MM/YYYY HH/MM/SSS)p.date',p.date)"));
        translated = new ScriptTranslator().translate("insert into x (1,2,:p.date);");
        assertTrue(translated.contains(",'p.date',p.date)"));

    }


}
