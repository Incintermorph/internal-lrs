
package com.example.lrs.mapper;

import com.example.lrs.domain.Statement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatementMapper {
    void insert(Map<String,Object> params);
    Statement findById(@Param("id") String id);
    List<Statement> query(@Param("since") OffsetDateTime since,
                          @Param("verbId") String verbId,
                          @Param("activityId") String activityId,
                          @Param("registration") String registration,
                          @Param("limit") int limit);
}
