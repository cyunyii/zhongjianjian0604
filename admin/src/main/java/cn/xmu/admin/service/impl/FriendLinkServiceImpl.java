package cn.xmu.admin.service.impl;

import cn.xmu.admin.repository.FriendLinkRepository;
import cn.xmu.admin.service.FriendLinkService;
import cn.xmu.enums.YesOrNo;
import cn.xmu.pojo.mo.FriendLinkMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendLinkServiceImpl implements FriendLinkService {

        @Autowired
        private FriendLinkRepository friendLinkRepository;

        @Override
        public void saveOrUpdateFriendLink(FriendLinkMO friendLinkMO) {

            friendLinkRepository.save(friendLinkMO);
        }

            @Override
            public List<FriendLinkMO> queryAllFriendLinkList() {
                return friendLinkRepository.findAll();
            }

            @Override
            public void delete(String linkId) {
                friendLinkRepository.deleteById(linkId);
            }

        @Override
        public List<FriendLinkMO> queryPortalAllFriendLinkList() {
            return friendLinkRepository.getAllByIsDelete(YesOrNo.NO.type);
        }

    }
