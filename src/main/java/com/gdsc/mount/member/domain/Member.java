package com.gdsc.mount.member.domain;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "members")
@Getter
public class Member {

    @Id
    private String _id;

    @Field(name = "username")
    @Indexed(unique = true)
    private String username;

    protected Member() {}
    public Member(String username) {
        this.username = username;
    }

    public void updateUsername(String username) {
        this.username = username;
    }
}
