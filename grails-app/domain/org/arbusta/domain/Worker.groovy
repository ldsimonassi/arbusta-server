package org.arbusta.domain

class Worker {
    // This data will be defined by arbusta depending on its internal organization.
    // In AMT this information is never used by the requester API.
    String lastName
    String firstName
    String email

    static constraints = {
    }
}
