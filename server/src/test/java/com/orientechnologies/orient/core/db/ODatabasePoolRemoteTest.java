package com.orientechnologies.orient.core.db;

import com.orientechnologies.common.io.OFileUtils;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.server.OServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class ODatabasePoolRemoteTest {
  private static final String SERVER_DIRECTORY = "./target/poolRemote";
  private OServer server;

  @Before
  public void before() throws Exception {
    OGlobalConfiguration.SERVER_BACKWARD_COMPATIBILITY.setValue(false);
    server = new OServer(false);
    server.setServerRootDirectory(SERVER_DIRECTORY);
    server.startup(
        getClass().getClassLoader().getResourceAsStream("com/orientechnologies/orient/server/network/orientdb-server-config.xml"));
    server.activate();

  }

  @Test
  public void testPoolCloseTx() {
    OrientDB orientDb = new OrientDB("remote:localhost:", "root", "root",
        OrientDBConfig.builder().addConfig(OGlobalConfiguration.DB_POOL_MAX, 1).build());

    if (!orientDb.exists("test")) {
      orientDb.create("test", ODatabaseType.MEMORY);
    }

    ODatabasePool pool = new ODatabasePool(orientDb, "test", "admin", "admin");
    ODatabaseDocument db = pool.acquire();
    db.createClass("Test");
    db.begin();
    db.save(new ODocument("Test"));
    db.close();
    db = pool.acquire();
    assertEquals(db.countClass("Test"), 0);

    pool.close();
    orientDb.close();
  }

  @After
  public void after() {

    server.shutdown();
    Orient.instance().shutdown();
    OFileUtils.deleteRecursively(new File(SERVER_DIRECTORY));
    Orient.instance().startup();
  }
}
