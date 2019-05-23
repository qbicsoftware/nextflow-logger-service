package life.qbic

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

@Requires(property="contact.first-name", defaultValue="Hubba")
@Requires(property='contact.last-name', defaultValue="Bubba")
@Requires(property='contact.email', defaultValue="hubba.bubby@bubblegum.com")
@Singleton
class Contact {

    String firstName

    String lastName

    String email

    Contact(@Property(name="contact.first-name") String firstName,
            @Property(name="contact.last-name") String lastName,
            @Property(name="contact.email") String email) {
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
    }

    @Override
    String toString() {
        StringBuilder builder = new StringBuilder()
        builder.append("--------\n")
        builder.append("$firstName $lastName\n")
        builder.append("$email\n")
        return builder.toString()
    }

}
