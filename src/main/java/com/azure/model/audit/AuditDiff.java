package com.azure.model.audit;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AuditDiff {
  private final Map<String, Map<String, Object>> payload = new LinkedHashMap<>();

  public AuditDiff put(String field, Object before, Object after) {
    Map<String, Object> entry = new LinkedHashMap<>();
    entry.put("before", before);
    entry.put("after",  after);
    payload.put(field, entry);
    return this;
  }

  public String toJson(ObjectMapper om) {
    try { return om.writeValueAsString(payload); }
    catch (Exception e) { return "{}"; }
  }

  public static AuditDiff of(String field, Object before, Object after) {
    return new AuditDiff().put(field, before, after);
  }
}