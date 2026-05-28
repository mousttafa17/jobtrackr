package com.moustafa.jobtrackr.application;

import com.moustafa.jobtrackr.user.User;
import org.springframework.data.jpa.domain.Specification;

public final class JobApplicationSpecifications {

    private JobApplicationSpecifications() {
    }

    public static Specification<JobApplication> belongsTo(User user) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user"), user);
    }

    public static Specification<JobApplication> hasStatus(ApplicationStatus status) {
        return (root, query, criteriaBuilder) -> status == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<JobApplication> companyContains(String company) {
        return (root, query, criteriaBuilder) -> isBlank(company)
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("companyName")),
                        containsPattern(company)
                );
    }

    public static Specification<JobApplication> searchContains(String search) {
        return (root, query, criteriaBuilder) -> {
            if (isBlank(search)) {
                return criteriaBuilder.conjunction();
            }

            String pattern = containsPattern(search);
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("companyName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("jobTitle")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("notes")), pattern)
            );
        };
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String containsPattern(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }
}
