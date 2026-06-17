package com.campus.biz.groupbuy.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.biz.groupbuy.entity.GbGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 拼团商品 Mapper
 */
@Mapper
public interface GbGoodsMapper extends BaseMapper<GbGoods> {

    /**
     * C 端商品分页：只查上架商品，LEFT JOIN 取每个商品的最低 SKU 价（min_price），
     * 支持按分类/标题筛选，按 sort 排序（newest / price_asc / price_desc）。
     * sort 取值由 Service 层白名单校验后传入，禁止直接拼接外部输入。
     */
    @Select("""
            <script>
            SELECT g.*, m.min_price AS minPrice
            FROM biz_gb_goods g
            LEFT JOIN (
                SELECT goods_id, MIN(original_price) AS min_price
                FROM biz_gb_sku
                WHERE deleted = 0 AND status = 1
                GROUP BY goods_id
            ) m ON m.goods_id = g.id
            WHERE g.deleted = 0 AND g.status = 1
            <if test="category != null and category != ''">
                AND g.category = #{category}
            </if>
            <if test="title != null and title != ''">
                AND g.title LIKE CONCAT('%', #{title}, '%')
            </if>
            ORDER BY
            <choose>
                <when test="sort == 'price_asc'">m.min_price ASC, g.id DESC</when>
                <when test="sort == 'price_desc'">m.min_price DESC, g.id DESC</when>
                <otherwise>g.id DESC</otherwise>
            </choose>
            </script>
            """)
    IPage<GbGoods> selectWebPage(Page<GbGoods> page,
                                 @Param("category") String category,
                                 @Param("title") String title,
                                 @Param("sort") String sort);

    /**
     * 上架商品的去重分类列表（供 C 端分类条）
     */
    @Select("""
            SELECT DISTINCT category FROM biz_gb_goods
            WHERE deleted = 0 AND status = 1 AND category IS NOT NULL AND category != ''
            ORDER BY category
            """)
    List<String> selectCategories();
}
