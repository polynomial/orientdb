/* Generated By:JJTree: Do not edit this line. OCreatePropertyStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.orientechnologies.orient.core.sql.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OCreatePropertyStatement extends OStatement {
  public OIdentifier className;
  public OIdentifier propertyName;
  public OIdentifier propertyType;
  public OIdentifier linkedType;
  public boolean unsafe = false;
  public List<OCreatePropertyAttributeStatement> attributes = new ArrayList<OCreatePropertyAttributeStatement>();

  public OCreatePropertyStatement(int id) {
    super(id);
  }

  public OCreatePropertyStatement(OrientSql p, int id) {
    super(p, id);
  }

  @Override
  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("CREATE PROPERTY ");
    className.toString(params, builder);
    builder.append(".");
    propertyName.toString(params, builder);
    builder.append(" ");
    propertyType.toString(params, builder);
    if (linkedType != null) {
      builder.append(" ");
      linkedType.toString(params, builder);
    }
    
    if (!attributes.isEmpty()) {
      builder.append(" (");
      for (int i = 0; i < attributes.size(); i++) {
        OCreatePropertyAttributeStatement att = attributes.get(i);
        builder.append(att.settingName.value);
        if (att.settingValue != null) {
          builder.append(" ");
          builder.append(att.settingValue.value);
        }
        
        if (i < attributes.size() - 1) {
          builder.append(", ");
        }
      }
      builder.append(")");
    }
    
    if (unsafe) {
      builder.append(" UNSAFE");
    }
  }
}
/* JavaCC - OriginalChecksum=ff78676483d59013ab10b13bde2678d3 (do not edit this line) */
