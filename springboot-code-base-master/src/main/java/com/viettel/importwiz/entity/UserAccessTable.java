package com.viettel.importwiz.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccessTable {
    private String username;

    private String datalakeType;

    private String tableCatalog;

    private String tableSchema;

    private String tableName;
}
