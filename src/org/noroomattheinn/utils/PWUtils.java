/*
 * PWUtils.java - Copyright(c) 2014 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: May 24, 2014
 */
package org.noroomattheinn.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.apache.commons.lang3.StringUtils;

/**
 * PWUtils: Vary basic utilities for storing passwords securely and comparing to
 * plaintext passwords.
 *
 * Based on code from this post:
 *      http://blog.jerryorr.com/2012/05/secure-password-storage-lots-of-donts.html
 * 
 * Usage scenario:
 * 1. User enters a password to be stored. This is entered as clear text in an app
 * 2. App generates a new salt value using generateSalt()
 * 3. App calls getEncryptedPassword() using the clear text password and the salt
 * 4. App stores both the salt and the encrypted password
 * 5. Sometime later a user ties to authenticate. The user sens their password
 *    in plain text
 * 6. The app retrieves the stored salt and encrypted password from storage
 * 7. The app calls authenticate to see if the provided password matches
 * 
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public class PWUtils {

    public boolean authenticate(
            String attemptedPassword, byte[] encryptedPassword, byte[] salt) {
        // Encrypt the clear-text password using the same salt that was used to
        // encrypt the original password
        byte[] encryptedAttemptedPassword = getEncryptedPassword(attemptedPassword, salt);

        // Authentication succeeds if encrypted password that the user entered
        // is equal to the stored hash
        return Arrays.equals(encryptedPassword, encryptedAttemptedPassword);
    }

    public byte[] getEncryptedPassword(String password, byte[] salt) {
        // PBKDF2 with SHA-1 as the hashing algorithm. Note that the NIST
        // specifically names SHA-1 as an acceptable hashing algorithm for PBKDF2
        String algorithm = "PBKDF2WithHmacSHA1";
        // SHA-1 generates 160 bit hashes, so that's what makes sense here
        int derivedKeyLength = 160;
        // Pick an iteration count that works for you. The NIST recommends at
        // least 1,000 iterations:
        // http://csrc.nist.gov/publications/nistpubs/800-132/nist-sp800-132.pdf
        // iOS 4.x reportedly uses 10,000:
        // http://blog.crackpassword.com/2010/09/smartphone-forensics-cracking-blackberry-backup-passwords/
        int iterations = 20000;

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength);

        try {
            SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);
            return f.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(PWUtils.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    public byte[] generateSalt() {
        // VERY important to use SecureRandom instead of just Random
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            // Generate a 8 byte (64 bit) salt as recommended by RSA PKCS5
            byte[] salt = new byte[8];
            random.nextBytes(salt);

            return salt;

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(PWUtils.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }
    
    public String externalRep(byte[] salt, byte[] encPassword) {
        String extSalt = Utils.toB64(salt);
        String extPwd = Utils.toB64(encPassword);
        return extSalt + "|" + extPwd;
    }
    
    public List<byte[]> internalRep(String externalRep) {
        byte[] salt = null;
        byte[] pwd = null;
        if (externalRep != null) {
            String[] vals = StringUtils.split(externalRep, '|');
            if (vals.length == 2) {
                String extSalt = vals[0];
                String extPwd = vals[1];
                salt = Utils.fromB64(extSalt);
                pwd = Utils.fromB64(extPwd);
            }
        }
        List<byte[]> result = new ArrayList<>();
        result.add(0, salt);
        result.add(1, pwd);
        return result;
    }
}