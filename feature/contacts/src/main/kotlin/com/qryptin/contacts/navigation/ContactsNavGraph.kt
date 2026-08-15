package com.qryptin.contacts.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.qryptin.core.designsystem.ui.theme.BottomNavDestination
import com.qryptin.contacts.ui.screens.ContactDetailScreen
import com.qryptin.contacts.ui.screens.EditContactScreen
import com.qryptin.contacts.model.Contact
import com.qryptin.contacts.ui.screens.ContactsScreen
import com.qryptin.contacts.ui.screens.addcontact.*
import com.qryptin.contacts.util.InviteIntentLauncher
import com.qryptin.contacts.viewmodel.ContactsViewModel
import com.qryptin.contacts.viewmodel.AddContactViewModel
import com.qryptin.contacts.viewmodel.AddContactNavEvent
import com.qryptin.ui.navigation.AppRoutes

// ─────────────────────────────────────────────────────────────
//  ContactsRoutes - Using centralized AppRoutes
// ─────────────────────────────────────────────────────────────
object ContactsRoutes {
    val CONTACTS                       = AppRoutes.Contacts.CONTACTS
    val CONTACT_DETAIL                 = AppRoutes.Contacts.CONTACT_DETAIL
    val EDIT_CONTACT                   = AppRoutes.Contacts.EDIT_CONTACT

    val ADD_CONTACT_PHONE              = AppRoutes.Contacts.ADD_CONTACT_PHONE
    val ADD_CONTACT_EXISTING           = AppRoutes.Contacts.ADD_CONTACT_EXISTING
    val ADD_CONTACT_REVIEW             = AppRoutes.Contacts.ADD_CONTACT_REVIEW
    val ADD_CONTACT_SUCCESS            = AppRoutes.Contacts.ADD_CONTACT_SUCCESS

    val ADD_CONTACT_NON_QRYPTIN        = AppRoutes.Contacts.ADD_CONTACT_NON_QRYPTIN
    val ADD_CONTACT_NON_QRYPTIN_REVIEW = AppRoutes.Contacts.ADD_CONTACT_NON_QRYPTIN_REVIEW

    fun contactDetail(id: String) = AppRoutes.Contacts.contactDetail(id)
    fun editContact(id: String) = AppRoutes.Contacts.editContact(id)
}

fun NavGraphBuilder.contactsGraph(
    navController   : NavHostController,
    currentDest     : BottomNavDestination,
    onNavSelected   : (BottomNavDestination) -> Unit,
    onChatClick     : (com.qryptin.contacts.model.Contact) -> Unit,
) {
    composable(ContactsRoutes.CONTACTS) {
        val viewModel: ContactsViewModel = viewModel()
        val context = LocalContext.current

        ContactsScreen(
            viewModel                = viewModel,
            onContactClick           = { contact ->
                navController.navigate(ContactsRoutes.contactDetail(contact.id))
            },
            onNewContactClick        = {
                navController.navigate(ContactsRoutes.ADD_CONTACT_PHONE)
            },
            onInviteContact          = { contact ->
                InviteIntentLauncher.launchSmsInvite(context, contact.phone)
            },
            onChatClick              = { contact ->
                onChatClick(contact)
            },
            currentNavDestination    = currentDest,
            onNavDestinationSelected = onNavSelected,
        )
    }

    composable(
        route     = ContactsRoutes.CONTACT_DETAIL,
        arguments = listOf(navArgument("contactId") { type = NavType.StringType }),
    ) { backStackEntry ->
        val contactId = backStackEntry.arguments?.getString("contactId").orEmpty()
        ContactDetailScreen(
            contactId           = contactId,
            onBack              = { navController.popBackStack() },
            onEdit              = { id -> navController.navigate(ContactsRoutes.editContact(id)) },
            onStartConversation = { contact -> onChatClick(contact) },
        )
    }

    composable(
        route     = ContactsRoutes.EDIT_CONTACT,
        arguments = listOf(navArgument("contactId") { type = NavType.StringType }),
    ) { backStackEntry ->
        val contactId = backStackEntry.arguments?.getString("contactId").orEmpty()
        EditContactScreen(
            contactId = contactId,
            onBack    = { navController.popBackStack() },
            onSaved   = { navController.popBackStack() },
        )
    }

    composable(ContactsRoutes.ADD_CONTACT_PHONE) { backStackEntry ->
        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry(ContactsRoutes.ADD_CONTACT_PHONE)
        }
        val addVm: AddContactViewModel = viewModel(parentEntry)

        LaunchedEffect(addVm) {
            addVm.navEvent.collect { event ->
                when (event) {
                    is AddContactNavEvent.NavigateToExistingContact ->
                        navController.navigate(ContactsRoutes.ADD_CONTACT_EXISTING)
                    is AddContactNavEvent.NavigateToNonQryptINContact ->
                        navController.navigate(ContactsRoutes.ADD_CONTACT_NON_QRYPTIN)
                    else -> Unit
                }
            }
        }

        AddContactPhoneScreen(
            viewModel = addVm,
            onBack    = { navController.popBackStack() },
            onCancel  = {
                navController.popBackStack(
                    route     = ContactsRoutes.CONTACTS,
                    inclusive = false,
                )
            },
        )
    }

    composable(ContactsRoutes.ADD_CONTACT_EXISTING) { backStackEntry ->
        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry(ContactsRoutes.ADD_CONTACT_PHONE)
        }
        val addVm: AddContactViewModel = viewModel(parentEntry)

        LaunchedEffect(addVm) {
            addVm.navEvent.collect { event ->
                when (event) {
                    is AddContactNavEvent.NavigateToReview ->
                        navController.navigate(ContactsRoutes.ADD_CONTACT_REVIEW)
                    else -> Unit
                }
            }
        }

        AddExistingQryptINContactScreen(
            viewModel = addVm,
            onBack    = { navController.popBackStack() },
            onCancel  = {
                navController.popBackStack(
                    route     = ContactsRoutes.CONTACTS,
                    inclusive = false,
                )
            },
            onNext = { /* navigation handled via navEvent */ },
        )
    }

    composable(ContactsRoutes.ADD_CONTACT_NON_QRYPTIN) { backStackEntry ->
        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry(ContactsRoutes.ADD_CONTACT_PHONE)
        }
        val addVm: AddContactViewModel = viewModel(parentEntry)

        LaunchedEffect(addVm) {
            addVm.navEvent.collect { event ->
                when (event) {
                    is AddContactNavEvent.NavigateToNonQryptINReview ->
                        navController.navigate(ContactsRoutes.ADD_CONTACT_NON_QRYPTIN_REVIEW)
                    else -> Unit
                }
            }
        }

        AddNonQryptINContactScreen(
            viewModel = addVm,
            onBack    = { navController.popBackStack() },
            onCancel  = {
                navController.popBackStack(
                    route     = ContactsRoutes.CONTACTS,
                    inclusive = false,
                )
            },
            onNext = { /* navigation handled via navEvent */ },
        )
    }

    composable(ContactsRoutes.ADD_CONTACT_REVIEW) { backStackEntry ->
        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry(ContactsRoutes.ADD_CONTACT_PHONE)
        }
        val addVm: AddContactViewModel = viewModel(parentEntry)

        LaunchedEffect(addVm) {
            addVm.navEvent.collect { event ->
                when (event) {
                    is AddContactNavEvent.NavigateToSuccess ->
                        navController.navigate(ContactsRoutes.ADD_CONTACT_SUCCESS) {
                            popUpTo(ContactsRoutes.ADD_CONTACT_PHONE) { inclusive = true }
                        }
                    else -> Unit
                }
            }
        }

        ReviewContactScreen(
            viewModel = addVm,
            onBack    = { navController.popBackStack() },
            onCancel  = {
                navController.popBackStack(
                    route     = ContactsRoutes.CONTACTS,
                    inclusive = false,
                )
            },
            onSaved = { /* navigation handled via navEvent */ },
        )
    }

    composable(ContactsRoutes.ADD_CONTACT_NON_QRYPTIN_REVIEW) { backStackEntry ->
        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry(ContactsRoutes.ADD_CONTACT_PHONE)
        }
        val addVm: AddContactViewModel = viewModel(parentEntry)
        val context = LocalContext.current

        LaunchedEffect(addVm) {
            addVm.navEvent.collect { event ->
                when (event) {
                    is AddContactNavEvent.NavigateToSuccess ->
                        navController.navigate(ContactsRoutes.ADD_CONTACT_SUCCESS) {
                            popUpTo(ContactsRoutes.ADD_CONTACT_PHONE) { inclusive = true }
                        }
                    is AddContactNavEvent.LaunchInvite ->
                        InviteIntentLauncher.launchInvite(context, event.method, event.phone, event.email)
                    else -> Unit
                }
            }
        }

        ReviewNonQryptINContactScreen(
            viewModel = addVm,
            onBack    = { navController.popBackStack() },
            onCancel  = {
                navController.popBackStack(
                    route     = ContactsRoutes.CONTACTS,
                    inclusive = false,
                )
            },
            onSaved = { /* navigation handled via navEvent */ },
        )
    }

    composable(ContactsRoutes.ADD_CONTACT_SUCCESS) {
        ContactSavedScreen(
            onViewContact = {
                navController.popBackStack(
                    route     = ContactsRoutes.CONTACTS,
                    inclusive = false,
                )
            },
        )
    }
}
