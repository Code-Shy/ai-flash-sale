package com.weijinchuan.aiflashsale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weijinchuan.aiflashsale.domain.StoreSku;
import com.weijinchuan.aiflashsale.vo.ProductDetailVO;
import com.weijinchuan.aiflashsale.vo.ProductListVO;
import com.weijinchuan.aiflashsale.vo.StoreSkuVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 门店商品 Mapper
 */
public interface StoreSkuMapper extends BaseMapper<StoreSku> {

    /**
     * 查询商品列表
     */
    @Select("""
        SELECT
            s.id AS skuId,
            s.sku_name AS skuName,
            s.specs AS specs,
            s.image_url AS imageUrl,
            p.category_name AS categoryName,
            MIN(ss.sale_price) AS salePrice
        FROM sku s
        JOIN spu p ON s.spu_id = p.id
        JOIN store_sku ss ON ss.sku_id = s.id
        WHERE s.status = 1
          AND p.status = 1
          AND ss.sale_status = 1
        GROUP BY s.id, s.sku_name, s.specs, s.image_url, p.category_name
        ORDER BY s.id DESC
        """)
    List<ProductListVO> listProducts();

    /**
     * 查询商品详情
     */
    @Select("""
        SELECT
            s.id AS skuId,
            s.sku_name AS skuName,
            s.specs AS specs,
            s.unit AS unit,
            s.image_url AS imageUrl,
            p.spu_name AS spuName,
            p.category_name AS categoryName,
            p.brand_name AS brandName,
            p.description AS description,
            MIN(ss.sale_price) AS lowestPrice
        FROM sku s
        JOIN spu p ON s.spu_id = p.id
        JOIN store_sku ss ON ss.sku_id = s.id
        WHERE s.id = #{skuId}
          AND s.status = 1
          AND p.status = 1
          AND ss.sale_status = 1
        GROUP BY s.id, s.sku_name, s.specs, s.unit, s.image_url,
                 p.spu_name, p.category_name, p.brand_name, p.description
        """)
    ProductDetailVO getProductDetail(@Param("skuId") Long skuId);

    /**
     * 查询门店商品列表
     */
    @Select("""
        SELECT
            ss.store_id AS storeId,
            s.id AS skuId,
            s.sku_name AS skuName,
            s.specs AS specs,
            s.image_url AS imageUrl,
            ss.sale_price AS salePrice,
            i.available_stock AS availableStock
        FROM store_sku ss
        JOIN sku s ON ss.sku_id = s.id
        LEFT JOIN inventory i ON i.store_id = ss.store_id AND i.sku_id = ss.sku_id
        WHERE ss.store_id = #{storeId}
          AND ss.sale_status = 1
          AND s.status = 1
        ORDER BY ss.sort DESC, ss.id DESC
        """)
    List<StoreSkuVO> listStoreSkus(@Param("storeId") Long storeId);
}