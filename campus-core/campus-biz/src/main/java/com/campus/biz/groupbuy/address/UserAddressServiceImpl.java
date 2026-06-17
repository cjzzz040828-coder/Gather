package com.campus.biz.groupbuy.address;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户收货地址 Service 实现
 */
@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress>
        implements UserAddressService {

    @Override
    public List<UserAddress> listMine(Long userId) {
        return this.list(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveAddress(UserAddress address, Long userId) {
        if (!StringUtils.hasText(address.getReceiver())) {
            throw new BusinessException("收货人不能为空");
        }
        if (!StringUtils.hasText(address.getPhone())) {
            throw new BusinessException("联系电话不能为空");
        }
        if (!StringUtils.hasText(address.getDetail())) {
            throw new BusinessException("详细地址不能为空");
        }
        address.setUserId(userId);
        if (address.getIsDefault() == null) {
            address.setIsDefault(0);
        }

        // 编辑场景：校验归属
        if (address.getId() != null) {
            UserAddress exist = this.getById(address.getId());
            if (exist == null || !userId.equals(exist.getUserId())) {
                throw new BusinessException("地址不存在");
            }
        }

        // 若设为默认，先清掉该用户其他默认
        if (Integer.valueOf(1).equals(address.getIsDefault())) {
            clearDefault(userId);
        }
        this.saveOrUpdate(address);
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMine(Long id, Long userId) {
        UserAddress exist = getMine(id, userId);
        if (exist == null) {
            throw new BusinessException("地址不存在");
        }
        this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id, Long userId) {
        UserAddress exist = getMine(id, userId);
        if (exist == null) {
            throw new BusinessException("地址不存在");
        }
        clearDefault(userId);
        this.update(new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getId, id)
                .set(UserAddress::getIsDefault, 1));
    }

    @Override
    public UserAddress getMine(Long id, Long userId) {
        UserAddress a = this.getById(id);
        if (a == null || !userId.equals(a.getUserId())) {
            return null;
        }
        return a;
    }

    private void clearDefault(Long userId) {
        this.update(new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .set(UserAddress::getIsDefault, 0));
    }
}
