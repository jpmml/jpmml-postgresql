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
import java.util.Arrays;

public class DecisionTreeIris_Species {

	static
	public String evaluate(ResultSet request) throws Exception {
		return (String)PMMLUtil.evaluateSimple(DecisionTreeIris.class, request);
	}

	static
	public String evaluate(double sepalLength, double sepalWidth, double petalLength, double petalWidth) throws Exception {
		return (String)PMMLUtil.evaluateSimple(DecisionTreeIris.class, Arrays.asList(sepalLength, sepalWidth, petalLength, petalWidth));
	}
}