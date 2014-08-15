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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.EvaluatorUtil;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.runtime.ModelEvaluatorCache;

public class PMMLUtil {

	private PMMLUtil(){
	}

	static
	public Object evaluateSimple(Class<?> clazz, Object request) throws Exception {

		if(request == null){
			return null;
		}

		Evaluator evaluator = getEvaluator(clazz);

		Map<FieldName, ?> arguments = loadArguments(evaluator, request);

		Map<FieldName, ?> result = evaluator.evaluate(arguments);

		Object targetValue = result.get(evaluator.getTargetField());

		return EvaluatorUtil.decode(targetValue);
	}

	static
	public boolean evaluateComplex(Class<?> clazz, Object request, ResultSet response) throws Exception {

		if(request == null){
			return false;
		}

		Evaluator evaluator = getEvaluator(clazz);

		Map<FieldName, ?> arguments = loadArguments(evaluator, request);

		Map<FieldName, ?> result = evaluator.evaluate(arguments);

		storeResult(evaluator, result, response);

		return true;
	}

	static
	private Map<FieldName, FieldValue> loadArguments(Evaluator evaluator, Object request) throws Exception {

		if(request instanceof ResultSet){
			return loadStruct(evaluator, (ResultSet)request);
		}

		return loadScalarList(evaluator, (List<?>)request);
	}

	static
	private Map<FieldName, FieldValue> loadStruct(Evaluator evaluator, ResultSet request) throws SQLException {
		Map<FieldName, FieldValue> result = Maps.newLinkedHashMap();

		Map<String, Integer> columns = parseColumns(request);

		Iterable<FieldName> fields = evaluator.getActiveFields();
		for(FieldName field : fields){
			String label = normalize(field.getValue());

			Integer column = columns.get(label);
			if(column == null){
				continue;
			}

			FieldValue value = EvaluatorUtil.prepare(evaluator, field, request.getObject(column));

			result.put(field, value);
		}

		return result;
	}

	static
	private Map<FieldName, FieldValue> loadScalarList(Evaluator evaluator, List<?> request){
		Map<FieldName, FieldValue> result = Maps.newLinkedHashMap();

		List<FieldName> fields = evaluator.getActiveFields();
		if(fields.size() != request.size()){
			throw new IllegalArgumentException("Invalid number of arguments");
		}

		int i = 0;

		for(FieldName field : fields){
			FieldValue value = EvaluatorUtil.prepare(evaluator, field, request.get(i));

			result.put(field, value);

			i++;
		}

		return result;
	}

	static
	private void storeResult(Evaluator evaluator, Map<FieldName, ?> result, ResultSet response) throws SQLException {
		Map<String, Integer> columns = parseColumns(response);

		Iterable<FieldName> fields = Iterables.concat(evaluator.getTargetFields(), evaluator.getOutputFields());
		for(FieldName field : fields){
			String label = normalize(field.getValue());

			Integer column = columns.get(label);
			if(column == null){
				continue;
			}

			Object value = EvaluatorUtil.decode(result.get(field));
			if(value != null){
				response.updateObject(column, value);
			} else

			{
				response.updateNull(column);
			}
		}
	}

	static
	private Map<String, Integer> parseColumns(ResultSet resultSet) throws SQLException {
		Map<String, Integer> result = Maps.newLinkedHashMap();

		ResultSetMetaData metaData = resultSet.getMetaData();

		for(int column = 1; column <= metaData.getColumnCount(); column++){
			String label = normalize(metaData.getColumnLabel(column));

			Integer previousColumn = result.put(label, column);
			if(previousColumn != null){
				throw new SQLException("Duplicate column label \"" + label + "\"");
			}
		}

		return result;
	}

	static
	private String normalize(String label){
		return label.toLowerCase();
	}

	static
	private Evaluator getEvaluator(Class<?> clazz) throws Exception {
		return PMMLUtil.evaluatorCache.get(clazz);
	}

	private static final ModelEvaluatorCache evaluatorCache = new ModelEvaluatorCache(CacheBuilder.newBuilder());
}