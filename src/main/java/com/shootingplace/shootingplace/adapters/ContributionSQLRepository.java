package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.contributions.ContributionEntity;
import com.shootingplace.shootingplace.contributions.ContributionRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContributionSQLRepository extends ContributionRepository, JpaRepository<ContributionEntity,String> {
}
