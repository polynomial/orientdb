/*
 * Copyright 2010-2016 OrientDB LTD (http://orientdb.com)
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
package com.orientechnologies.orient.test.database.auto;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OTodoResultSet;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO re-enable when the new executor is implemented in remote
@Test(enabled = false) @SuppressWarnings("unused") public class TraverseTestNew extends DocumentDBBaseTest {
  private int totalElements = 0;
  private ODocument tomCruise;
  private ODocument megRyan;
  private ODocument nicoleKidman;

  @Parameters(value = "url") public TraverseTestNew(@Optional String url) {
    super(url);
  }

  @BeforeClass public void init() {
    OrientGraph graph = new OrientGraph(database);
    graph.setUseLightweightEdges(false);

    graph.createVertexType("Movie");
    graph.createVertexType("Actor");

    tomCruise = graph.addVertex("class:Actor", "name", "Tom Cruise").getRecord();
    totalElements++;
    megRyan = graph.addVertex("class:Actor", "name", "Meg Ryan").getRecord();
    totalElements++;
    nicoleKidman = graph.addVertex("class:Actor", "name", "Nicole Kidman", "attributeWithDotValue", "a.b").getRecord();
    totalElements++;

    ODocument topGun = graph.addVertex("class:Movie", "name", "Top Gun", "year", 1986).getRecord();
    totalElements++;
    ODocument missionImpossible = graph.addVertex("class:Movie", "name", "Mission: Impossible", "year", 1996).getRecord();
    totalElements++;
    ODocument youHaveGotMail = graph.addVertex("class:Movie", "name", "You've Got Mail", "year", 1998).getRecord();
    totalElements++;

    graph.addEdge(null, graph.getVertex(tomCruise), graph.getVertex(topGun), "actorIn");
    totalElements++;
    graph.addEdge(null, graph.getVertex(megRyan), graph.getVertex(topGun), "actorIn");
    totalElements++;
    graph.addEdge(null, graph.getVertex(tomCruise), graph.getVertex(missionImpossible), "actorIn");
    totalElements++;
    graph.addEdge(null, graph.getVertex(megRyan), graph.getVertex(youHaveGotMail), "actorIn");
    totalElements++;

    graph.addEdge(null, graph.getVertex(tomCruise), graph.getVertex(megRyan), "friend");
    totalElements++;
    graph.addEdge(null, graph.getVertex(tomCruise), graph.getVertex(nicoleKidman), "married").setProperty("year", 1990);
    totalElements++;

    graph.commit();
  }

  public void traverseSQLAllFromActorNoWhereBreadthFrirst() {
    OTodoResultSet result1 = database.query("traverse * from " + tomCruise.getIdentity() + " strategy BREADTH_FIRST");

    for (int i = 0; i < totalElements; i++) {
      Assert.assertTrue(result1.hasNext());
      result1.next();
    }
  }

  public void traverseSQLAllFromActorNoWhereDepthFrirst() {
    OTodoResultSet result1 = database.query("traverse * from " + tomCruise.getIdentity() + " strategy DEPTH_FIRST");

    for (int i = 0; i < totalElements; i++) {
      Assert.assertTrue(result1.hasNext());
      result1.next();
    }
  }

  @Test public void traverseSQLOutFromActor1Depth() {
    OTodoResultSet result1 = database.query("traverse out_ from " + tomCruise.getIdentity() + " while $depth <= 1");

    Assert.assertTrue(result1.hasNext());
  }

  @Test public void traverseSQLMoviesOnly() {
    OTodoResultSet result1 = database.query("select from ( traverse * from Movie ) where @class = 'Movie'");
    Assert.assertTrue(result1.hasNext());
    while (result1.hasNext()) {
      ODocument d = (ODocument) result1.next().getElement();
      Assert.assertEquals(d.getClassName(), "Movie");
    }
  }

  @Test public void traverseSQLPerClassFields() {
    OTodoResultSet result1 = database
        .query("select from ( traverse out() from " + tomCruise.getIdentity() + ") where @class = 'Movie'");
    Assert.assertTrue(result1.hasNext());
    while (result1.hasNext()) {
      ODocument d = ((OIdentifiable) result1.next().getElement()).getRecord();
      Assert.assertEquals(d.getClassName(), "Movie");
    }
  }

  @Test public void traverseSQLMoviesOnlyDepth() {
    OTodoResultSet result1 = database
        .query("select from ( traverse * from " + tomCruise.getIdentity() + " while $depth <= 1 ) where @class = 'Movie'");
    Assert.assertFalse(result1.hasNext());

    OTodoResultSet result2 = database
        .query("select from ( traverse * from " + tomCruise.getIdentity() + " while $depth <= 2 ) where @class = 'Movie'");
    Assert.assertTrue(result2.hasNext());
    int size2 = 0;
    while (result2.hasNext()) {
      ODocument d = result2.next().getElement().getRecord();
      Assert.assertEquals(d.getClassName(), "Movie");
      size2++;
    }

    OTodoResultSet result3 = database
        .query("select from ( traverse * from " + tomCruise.getIdentity() + " ) where @class = 'Movie'");
    Assert.assertTrue(result3.hasNext());
    int size3 = 0;
    while (result3.hasNext()) {
      ODocument d = result3.next().getElement().getRecord();
      Assert.assertEquals(d.getClassName(), "Movie");
      size3++;
    }
    Assert.assertTrue(size3 > size2);
  }

  @Test public void traverseSelect() {
    OTodoResultSet result1 = database.query("traverse * from ( select from Movie )");
    int tot = 0;
    while (result1.hasNext()) {
      result1.next();
      tot++;
    }

    Assert.assertEquals(tot, totalElements);
  }

  @Test public void traverseSQLSelectAndTraverseNested() {
    OTodoResultSet result1 = database.query("traverse * from ( select from ( traverse * from " + tomCruise.getIdentity()
        + " while $depth <= 2 ) where @class = 'Movie' )");

    int tot = 0;
    while (result1.hasNext()) {
      result1.next();
      tot++;
    }

    Assert.assertEquals(tot, totalElements);
  }

  @Test public void traverseAPISelectAndTraverseNested() {
    OTodoResultSet result1 = database.command("traverse * from ( select from ( traverse * from " + tomCruise.getIdentity()
        + " while $depth <= 2 ) where @class = 'Movie' )");
    int tot = 0;
    while (result1.hasNext()) {
      result1.next();
      tot++;
    }
    Assert.assertEquals(tot, totalElements);
  }

  @Test public void traverseAPISelectAndTraverseNestedDepthFirst() {
    OTodoResultSet result1 = database.query("traverse * from ( select from ( traverse * from " + tomCruise.getIdentity()
        + " while $depth <= 2 strategy depth_first ) where @class = 'Movie' )");
    int tot = 0;
    while (result1.hasNext()) {
      result1.next();
      tot++;
    }
    Assert.assertEquals(tot, totalElements);
  }

  @Test public void traverseAPISelectAndTraverseNestedBreadthFirst() {
    OTodoResultSet result1 = database.command("traverse * from ( select from ( traverse * from " + tomCruise.getIdentity()
        + " while $depth <= 2 strategy breadth_first ) where @class = 'Movie' )");
    int tot = 0;
    while (result1.hasNext()) {
      result1.next();
      tot++;
    }
    Assert.assertEquals(tot, totalElements);
  }

  @Test public void traverseSelectNoInfluence() {
    OTodoResultSet result1 = database.query("traverse * from Movie while $depth < 2");
    List<OResult> list1 = new ArrayList<>();
    while (result1.hasNext()) {
      list1.add(result1.next());
    }
    OTodoResultSet result2 = database.query("select from ( traverse * from Movie while $depth < 2 )");
    List<OResult> list2 = new ArrayList<>();
    while (result2.hasNext()) {
      list2.add(result2.next());
    }
    OTodoResultSet result3 = database.query("select from ( traverse * from Movie while $depth < 2 ) where true");
    List<OResult> list3 = new ArrayList<>();
    while (result3.hasNext()) {
      list3.add(result3.next());
    }
    OTodoResultSet result4 = database
        .query("select from ( traverse * from Movie while $depth < 2 and ( true = true ) ) where true");

    List<OResult> list4 = new ArrayList<>();
    while (result4.hasNext()) {
      list4.add(result4.next());
    }

    Assert.assertEquals(list1, list2);
    Assert.assertEquals(list1, list3);
    Assert.assertEquals(list1, list4);
  }

  @Test public void traverseNoConditionLimit1() {
    OTodoResultSet result1 = database.query("traverse * from Movie limit 1");
    Assert.assertTrue(result1.hasNext());
    result1.next();
    Assert.assertFalse(result1.hasNext());
  }

  @Test public void traverseAndFilterByAttributeThatContainsDotInValue() {
    // issue #4952
    OTodoResultSet result1 = database
        .query("select from ( traverse out_married, in[attributeWithDotValue = 'a.b']  from " + tomCruise.getIdentity() + ")");
    Assert.assertTrue(result1.hasNext());
    boolean found = false;
    while (result1.hasNext()) {
      OResult doc = result1.next();
      String name = doc.getProperty("name");
      if ("Nicole Kidman".equals(name)) {
        found = true;
        break;
      }
    }
    Assert.assertTrue(found);
  }

  @Test public void traverseAndFilterWithNamedParam() {
    // issue #5225
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("param1", "a.b");
    OTodoResultSet result1 = database
        .query("select from (traverse out_married, in[attributeWithDotValue = :param1]  from " + tomCruise.getIdentity() + ")",
            params);
    Assert.assertTrue(result1.hasNext());
    boolean found = false;
    while (result1.hasNext()) {
      OResult doc = result1.next();
      String name = doc.getProperty("name");
      if ("Nicole Kidman".equals(name)) {
        found = true;
        break;
      }
    }
    Assert.assertTrue(found);
  }

  @Test public void traverseAndCheckDepthInSelect() {
    OTodoResultSet result1 = database
        .query("select *, $depth as d from ( traverse out_married  from " + tomCruise.getIdentity() + " while $depth < 2)");
    boolean found = false;
    Integer i = 0;
    while (result1.hasNext()) {
      OResult doc = result1.next();
      Integer depth = doc.getProperty("d");
      Assert.assertEquals(depth, i++);
    }
    Assert.assertEquals(i.intValue(), 2);
  }

  @Test public void traverseAndCheckReturn() {

    try {

      String q = "traverse in('married')  from " + nicoleKidman.getIdentity() + "";
      ODatabaseDocumentTx db = (ODatabaseDocumentTx) database.copy();
      ODatabaseRecordThreadLocal.INSTANCE.set(db);
      OTodoResultSet result1 = db.query(q);
      Assert.assertTrue(result1.hasNext());
      boolean found = false;
      Integer i = 0;
      OResult doc;
      while (result1.hasNext()) {
        doc = result1.next();
        i++;
      }
      Assert.assertEquals(i.intValue(), 2);
    } finally {
      ODatabaseRecordThreadLocal.INSTANCE.set(database);
    }

  }

}