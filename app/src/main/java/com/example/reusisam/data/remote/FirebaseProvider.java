package com.example.reusisam.data.remote;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Punto de acceso a los servicios Firebase. La inicialización se realiza
 * automáticamente a partir de google-services.json.
 */
public final class FirebaseProvider {

    private FirebaseProvider() {
    }

    public static FirebaseFirestore firestore() {
        return FirebaseFirestore.getInstance();
    }
}
