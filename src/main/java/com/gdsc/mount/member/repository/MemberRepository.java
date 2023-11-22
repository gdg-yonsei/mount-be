package com.gdsc.mount.member.repository;

import com.gdsc.mount.member.domain.Member;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends MongoRepository<Member, String> {
    Optional<Member> findByUsername(String username);
    boolean existsByUsername(String username);
}
