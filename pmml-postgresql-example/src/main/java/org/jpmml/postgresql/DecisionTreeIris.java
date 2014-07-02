/*
 * Copyright (c) 2014 Villu Ruusmann
 *
 * This file is part of JPMML-PostgreSQL
 *
 * JPMML-PostgreSQL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-PostgreSQL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-PostgreSQL.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpmml.postgresql;

import java.sql.ResultSet;

public class DecisionTreeIris {

	static
	public String evaluate(ResultSet request) throws Exception {
		return (String)PMMLUtil.evaluate(DecisionTreeIris.class, request);
	}

	static
	public boolean evaluate(ResultSet request, ResultSet response) throws Exception {
		return PMMLUtil.evaluate(DecisionTreeIris.class, request, response);
	}
}