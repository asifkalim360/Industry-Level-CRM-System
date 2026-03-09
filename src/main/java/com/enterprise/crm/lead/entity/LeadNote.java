//Reason: Lead module se related sab entity lead.entity folder me rakhte hain.
// Enterprise project me feature-based packaging hota hai.
package com.enterprise.crm.lead.entity;

import com.enterprise.crm.common.audit.BaseEntity;
import jakarta.persistence.*;

// Iska matlab: Ye class database table banegi.
//Jab Spring Boot + JPA run karega to Hibernate samjhega: (LeadNote class -> lead_notes table-name hoga)
@Entity

// Ye explicitly table ka naam define kar raha hai. Database table name-> lead_notes
//Agar ye annotation na hota to default table naam hota -> lead_note
//Isliye production project me explicit naming prefer karte hain.
@Table(name = "lead_notes")

// Matlab: LeadNote -> BaseEntity class ko extend kar raha hai. BaseEntity class usually hoti hai:(created_at, updated_at, created_by, updated_by).
// Example: Note kab add hua, Note kis user ne add kiya, Note kab update hua
//Isliye audit purpose ke liye use hota hai.Enterprise systems me ye bahut important hota hai.
public class LeadNote extends BaseEntity {

    @Id     // Ye column primary key hai. Example:(1,2,3,4)
    // @GeneratedValue -> Database automatically id generate karega.
    // GenerationType.IDENTITY: Auto increment -> MySQL me aise kaam karta hai.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ye lead ka comment / note store karega.
    //Example: Customer interested in premium package, Call after 2 days, Demo scheduled
    // length = 3000,Matlab database column max 3000 characters Allowed hai.
    //Agar ye na likho to default length 255 hota hai.Isliye long notes store karne ke liye length increase kiya.
    @Column(length = 3000)
    private String note;

    // Yaha Lead aur LeadNote ka relationship define ho raha hai.
    //Samjho scenario: 1 Lead ke multiple notes ho sakte hain
    //Example Lead: Rahul (Note1: call kiya, Note2: meeting scheduled, Note3: proposal bheja)
    // Matlab relation Lead -> Many Notes | Isliye relation hoga ManyToOne

    // FetchType.LAZY -> Matlab LeadNote fetch karte waqt Lead object automatically load nahi hoga
    //Example: Query -> SELECT * FROM lead_notes -> Ye sirf notes fetch karega.
    //Lead tab load hoga jab humlog use karenge | Example (note.getLead()) | Isse performance improve hoti hai.
    //Enterprise systems me LAZY fetch preferred hota hai.

    @ManyToOne(fetch = FetchType.LAZY)

    // @JoinColumn -> Ye database column create karega : lead_id
    //Table structure: lead_notes (id, note, lead_id, created_at, updated_at)
    //Example data
    //id | note                | lead_id
    //-----------------------------------
    //1  | called customer     | 5
    //2  | meeting scheduled   | 5
    //3  | demo completed      | 5
    //Matlab: ye notes lead_id 5 ke hain.
    @JoinColumn(name = "lead_id")

    // Iska matlab: LeadNote -> Lead entity se connected hai
    // Java object level relation : LeadNote -> Lead
    // to Database level relation : lead_id -> leads.id
    private Lead lead;
}
// Real CRM Flow
// Jab sales executive lead pe kaam karta hai to notes add karta hai.
//Example -> Lead: Rahul Sharma
//Activity log:
//Note1 -> called customer
//Note2 -> meeting scheduled
//Note3 -> proposal shared
//Isliye LeadNote entity banayi jati hai.
//------------------------------------------------------------

// INTERVIEW
// "LeadNote entity CRM system me lead ke saath related activity notes store karne ke liye use hoti hai.
// Ek lead ke multiple notes ho sakte hain, isliye LeadNote aur Lead ke beech ManyToOne relationship use kiya gaya hai.
// Entity Auditable base class extend karti hai jisse note creation aur update timestamps automatically track ho sake."
