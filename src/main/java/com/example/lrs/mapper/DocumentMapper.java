
package com.example.lrs.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.Map;

@Mapper
public interface DocumentMapper {
    void upsert(Map<String,Object> params);
    Map<String,Object> findOne(Map<String,Object> params);
    int delete(Map<String,Object> params);
}
