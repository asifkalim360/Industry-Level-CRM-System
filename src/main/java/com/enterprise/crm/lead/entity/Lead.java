package com.enterprise.crm.lead.entity;

// User entity ko import kar raha hai. Kyuki lead kisi user ko assign hota hai
// Example: Lead -> Sales Executive, Lead -> Manager
// Isliye relation banega:  Lead → User
import com.enterprise.crm.auth.entity.User;

// Ye audit base class hai. Isme usually ye fields hoti hain: createdAt, updatedAt, createdBy, updatedBy.
// Example: Lead create kab hua, Lead update kab hua, Lead kisne create kiya
// Isliye Lead class extend kar rahi hai: (public class Lead extends BaseEntity).
import com.enterprise.crm.common.audit.BaseEntity;

// Ye ENUM classes hain:- Example: LeadStatus -> NEW, CONTACTED, QUALIFIED, LOST, WON
// LeadSource -> WEBSITE, FACEBOOK, GOOGLE_ADS, REFERRAL
// Iska fayda: ❌ galat value nahi ja sakti: status = "abc", ✅ sirf valid value: status = NEW
import com.enterprise.crm.lead.enums.LeadSource;
import com.enterprise.crm.lead.enums.LeadStatus;

// Ye JPA annotations provide karta hai.
// Example: @Entity, @Table, @Id, @Column, @ManyToOne
// Ye sab database mapping ke liye use hote hain.
import jakarta.persistence.*;

// Ye Lombok library hai: Ye automatically generate karti hai:
// Getter, Setter, Constructor, Builder: Matlab tumhe manually code nahi likhna padta.
import lombok.*;
import org.hibernate.Hibernate;

// Ye date store karne ke liye hai. Example: Follow up date
import java.time.LocalDate;

// Matlab: Ye class database table banegi: Hibernate/JPA samajh jata hai -> (Lead class -> leads table).
@Entity
// Automatically generate karega: getId(), setId(), getEmail(), setEmail()
@Getter
@Setter
// Object creation easy ho jata hai.
// Example: Lead lead = Lead.builder().fullName("Asif").email("asif@gmail.com").status(LeadStatus.NEW).build();
//Enterprise projects me Builder pattern preferred hota hai.
@Builder
@Table(name = "leads",  //Matlab: Database me table ka naam -> leads
    indexes = {
        // Index ka matlab: Search fast ho jata hai: Example query: findByEmail() => Index hone se query fast chalegi.
        @Index(name = "idx_lead_email", columnList = "email"),
        // Ye useful hai jab hmog filter karenge, Example: get all leads where status = NEW
        @Index(name = "idx_lead_status", columnList = "status"),
        // Example query: get all leads assigned to user
        @Index(name = "idx_lead_assigned", columnList = "assigned_to")
    }
)
@NoArgsConstructor      // Empty constructor: Hibernate ko chahiye hota hai.
@AllArgsConstructor     // All field constructor, Example: new Lead(id,name,email,...)
// Matlab Lead class BaseEntity inherit kar rahi hai.
//Isliye Lead table me automatically aayega: (created_at, updated_at, created_by, updated_by)
public class Lead extends BaseEntity {

    @Id  // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto increment: Database automatically id generate karega
    private Long id;

    // Matlab: null allowed nahi hai | Database column: full_name | Example: Rahul Sharma, Aman Verma
    @Column(nullable = false)
    private String fullName;

    // Rules: null save nahi hoga | duplicate nahi hoga | Example: ❌ allowed nahi (a@gmail.com, a@gmail.com)
    @Column(nullable = false, unique = true)
    private String email;

    // Optional field.
    private String phone;

    // Lead kis company se hai | Example: TCS, Infosys, Flipkart
    private String company;

    // Enum store karega database me | Example: NEW, CONTACTED, QUALIFIED
    // Agar ENUM use na kiya to problem hoti | status = "abc"
    // EnumType.STRING : Database me value aise store hogi (NEW, CONTACTED). ||WARNING->  NOT USE THIS VALUES (1,2,3)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadStatus status;

    // Example: (WEBSITE, FACEBOOK, REFERRAL)
    @Enumerated(EnumType.STRING)
    private LeadSource source;

    // Example: next call date, meeting date, demo date
    private LocalDate followUpDate;

    // Lead ke notes : Length 2000 characters tak allowed.
    //Example: Customer interested in premium plan, Call again after 3 days.
    @Column(length = 2000)
    private String description;

    // Assigned user (Manager/Admin etc.)
    // @ManyToOne -> Lead usually assign hota hai. 1 Lead -> 1 User
    @ManyToOne(fetch = FetchType.LAZY) // Matlab:User data tabhi load hoga jab zarurat ho.Performance improve hoti hai.
    @JoinColumn(name="assigned_to")    // Database column: assigned_to. | Example: assigned_to = 5 | Matlab: User id = 5
    private User assignedTo;

    // Soft delete ka matlab: Lead database se delete nahi hoga.Bas flag change hoga : deleted = true
    //Benefit -> data recover ho sakta hai AND audit possible hai.
    @Column(nullable = false)
    private boolean deleted = false;

}
// "Lead entity CRM system ka core entity hai jo potential customers ko represent karti hai.
// Isme lead details, status tracking, source information, follow-up scheduling aur user assignment ka support diya gaya hai.
// Entity audit base class extend karti hai jisse automatically created and updated metadata maintain hota hai.
// Performance optimize karne ke liye email, status aur assigned user par indexes lagaye gaye hain."