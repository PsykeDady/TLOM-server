package it.tlom.journey;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table(name = "package_installation")
class PackageInstallationEntity {
    @Id String id;
    @Column(name = "player_id") String playerId;
    @Column(name = "package_id") String packageId;
    @Column(name = "package_version") String packageVersion;
    @Column(name = "configuration_json") String configurationJson;

    protected PackageInstallationEntity() { }

    PackageInstallationEntity(String id, String playerId, String packageId, String packageVersion, String configurationJson) {
        this.id = id;
        this.playerId = playerId;
        this.packageId = packageId;
        this.packageVersion = packageVersion;
        this.configurationJson = configurationJson;
    }
}