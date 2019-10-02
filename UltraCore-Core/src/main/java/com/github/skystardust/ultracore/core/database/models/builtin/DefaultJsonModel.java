package com.github.skystardust.ultracore.core.database.models.builtin;

import com.github.skystardust.ultracore.core.database.models.UltraCoreBaseModel;
import com.github.skystardust.ultracore.core.utils.FileUtils;
import io.ebean.EbeanServer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "jsons_table")
public class DefaultJsonModel extends UltraCoreBaseModel {

    private EbeanServer ebeanServer;

    @Id
    private String name;
    @Column(columnDefinition = "TEXT")
    private String content;

    public DefaultJsonModel(EbeanServer ebeanServer, String name, Object content) {
        this.ebeanServer = ebeanServer;
        this.name = name;
        this.content = FileUtils.GSON.toJson(content);
    }

    public static Optional<String> getContentByName(EbeanServer ebeanServer, String name) {
        return ebeanServer.find(DefaultJsonModel.class)
                .where()
                .eq("name", name)
                .findOneOrEmpty()
                .map(DefaultJsonModel::getContent);
    }


    @Override
    public EbeanServer modelEbeanServer() {
        return ebeanServer;
    }
}
