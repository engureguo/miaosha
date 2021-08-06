package com.engure.miaosha.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true) // 链式调用
public class Stock {
    private Integer id;
    private String name;
    private Integer total;
    private Integer sale;
    private Integer version;
}
